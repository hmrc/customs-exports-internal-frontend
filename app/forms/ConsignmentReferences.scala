/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import forms.ConsignmentReferenceType.ConsignmentReferenceType
import forms.EnhancedMapping.requiredRadio
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Format, Json, Reads, Writes}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.FieldValidator._

case class ConsignmentReferences(reference: ConsignmentReferenceType, referenceValue: String)

object ConsignmentReferenceType extends Enumeration {
  type ConsignmentReferenceType = Value
  val D, M, DP = Value
  implicit val format: Format[ConsignmentReferenceType] = Format(Reads.enumNameReads(ConsignmentReferenceType), Writes.enumNameWrites)
}

object ConsignmentReferences {

  def apply(ucrType: String, ucr: String): ConsignmentReferences =
    ucrType match {
      case "D"  => new ConsignmentReferences(ConsignmentReferenceType.D, ucr)
      case "M"  => new ConsignmentReferences(ConsignmentReferenceType.M, ucr)
      case "DP" => new ConsignmentReferences(ConsignmentReferenceType.DP, ucr)
    }

  implicit val format = Json.format[ConsignmentReferences]

  val formId = "ConsignmentReferences"

  private def form2Model: (ConsignmentReferenceType, Option[String], Option[String]) => ConsignmentReferences = {
    case (reference, ducrValue, mucrValue) =>
      (reference: @unchecked) match {
        case ConsignmentReferenceType.D => ConsignmentReferences(ConsignmentReferenceType.D, ducrValue.fold("")(_.trim.toUpperCase))
        case ConsignmentReferenceType.M => ConsignmentReferences(ConsignmentReferenceType.M, mucrValue.fold("")(_.trim.toUpperCase))
      }
  }

  private def model2Form: ConsignmentReferences => Option[(ConsignmentReferenceType, Option[String], Option[String])] =
    model =>
      model.reference match {
        case ConsignmentReferenceType.D => Some((model.reference, Some(model.referenceValue), None))
        case ConsignmentReferenceType.M => Some((model.reference, None, Some(model.referenceValue)))
        case _                          => Some((model.reference, None, None))
      }

  val mapping = Forms
    .mapping(
      "reference" -> requiredRadio("consignmentReferences.reference.empty")
        .verifying("consignmentReferences.reference.error", isContainedIn[ConsignmentReferenceType](ConsignmentReferenceType.values, _.toString))
        .transform[ConsignmentReferenceType](ConsignmentReferenceType.withName, _.toString),
      "ducrValue" -> mandatoryIfEqual(
        "reference",
        ConsignmentReferenceType.D.toString,
        text()
          .verifying("consignmentReferences.reference.ducrValue.empty", nonEmpty)
          .verifying("consignmentReferences.reference.ducrValue.error", isEmpty or validDucrIgnoreCase)
      ),
      "mucrValue" -> mandatoryIfEqual(
        "reference",
        ConsignmentReferenceType.M.toString,
        text()
          .verifying("consignmentReferences.reference.mucrValue.empty", nonEmpty)
          .verifying("consignmentReferences.reference.mucrValue.error", isEmpty or validMucrIgnoreCase)
      )
    )(form2Model)(model2Form)

  def form(): Form[ConsignmentReferences] = Form(mapping)

}
