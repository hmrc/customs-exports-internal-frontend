/*
 * Copyright 2024 HM Revenue & Customs
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

import models.UcrType._
import models.{UcrBlock, UcrType}
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.FieldValidator._

case class AssociateUcr(kind: UcrType, ucr: String)

object AssociateUcr {
  val formId: String = "AssociateDucr"

  implicit val format: OFormat[AssociateUcr] = Json.format[AssociateUcr]

  val mapping: Mapping[AssociateUcr] = {
    def bind(associateKind: UcrType, ducr: Option[String], mucr: Option[String]): AssociateUcr =
      associateKind match {
        case Mucr => AssociateUcr(Mucr, mucr.get.trim.toUpperCase)
        case _    => AssociateUcr(Ducr, ducr.get.trim.toUpperCase)
      }

    def unbind(value: AssociateUcr): Option[(UcrType, Option[String], Option[String])] =
      value.kind match {
        case Ducr => Some((value.kind, Some(value.ucr), None))
        case Mucr => Some((value.kind, None, Some(value.ucr)))
        case _    => None
      }

    Forms.mapping(
      "kind" -> of[UcrType](UcrType.formatter),
      "ducr" -> mandatoryIfEqual("kind", Ducr.formValue, text().verifying("ducr.error.format", validDucrIgnoreCase)),
      "mucr" -> mandatoryIfEqual("kind", Mucr.formValue, text().verifying("mucr.error.format", validMucrIgnoreCase))
    )(bind)(unbind)
  }

  val form: Form[AssociateUcr] = Form(mapping)

  def apply(ucrBlock: UcrBlock): AssociateUcr =
    AssociateUcr(
      ucr = ucrBlock.ucr,
      kind = (ucrBlock.ucrType: @unchecked) match {
        case Ducr.codeValue => Ducr
        case Mucr.codeValue => Mucr
      }
    )
}
