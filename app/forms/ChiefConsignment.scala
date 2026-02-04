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

import forms.ChiefConsignment._
import models.{UcrBlock, UcrType}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.Condition
import utils.FieldValidator._

case class ChiefConsignment(mucr: Option[String], ducr: Option[String], ducrPartId: Option[String]) {

  def toUcrBlock: UcrBlock = this match {
    case ChiefConsignment(Some(mucr), None, None) => UcrBlock(ucr = mucr, ucrType = UcrType.Mucr.codeValue, chiefUcr = Some(true))
    case ChiefConsignment(None, Some(ducr), None) => UcrBlock(ucr = ducr, ucrType = UcrType.Ducr.codeValue, chiefUcr = Some(true))
    case ChiefConsignment(None, Some(ducr), Some(ducrPartId)) =>
      UcrBlock(ucr = s"$ducr$Separator$ducrPartId", ucrType = UcrType.DucrPart.codeValue, chiefUcr = Some(true))
    case _ => throw new IllegalArgumentException(s"Cannot create valid UcrBlock instance from ChiefConsignment: [${this.toString}]")
  }
}

object ChiefConsignment {
  implicit val format: OFormat[ChiefConsignment] = Json.format[ChiefConsignment]

  val Separator = "-"

  def apply(ucrBlock: UcrBlock): ChiefConsignment =
    ucrBlock.ucrType match {
      case UcrType.DucrPart.codeValue =>
        val separatorIndex = ucrBlock.ucr.lastIndexOf(Separator)
        val (ducr, ducrPartId) = ucrBlock.ucr.splitAt(separatorIndex)
        val ducrPartIdWithoutSeparator = ducrPartId.tail

        ChiefConsignment(mucr = None, ducr = Some(ducr), ducrPartId = Some(ducrPartIdWithoutSeparator))
      case UcrType.Ducr.codeValue => ChiefConsignment(mucr = None, ducr = Some(ucrBlock.ucr), ducrPartId = None)
      case UcrType.Mucr.codeValue => ChiefConsignment(mucr = Some(ucrBlock.ucr), ducr = None, ducrPartId = None)
      case _ => throw new IllegalArgumentException(s"Cannot create ChiefConsignment instance from UcrBlock of type: [${ucrBlock.ucrType}]")
    }

  val mapping: Mapping[ChiefConsignment] = {
    val mucrField = "mucr"
    val ducrField = "ducr"
    val ducrPartField = "ducrPartId"

    def isFieldEmpty(field: String): Condition = _.get(field).forall(_.isEmpty())
    def isFieldNotEmpty(field: String): Condition = _.get(field).exists(_.nonEmpty)
    val nonEmptyOptionString = (input: Option[String]) => nonEmpty(input.getOrElse(""))

    def mucrMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldNotEmpty(mucrField) and (isFieldEmpty(ducrField) and isFieldEmpty(ducrPartField)),
          "manageChiefConsignment.mucr.error",
          validMucrIgnoreCaseOption
        )
      )
    )

    def ducrMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(isFieldNotEmpty(ducrField) and isFieldEmpty(mucrField), "manageChiefConsignment.ducr.error", validDucrIgnoreCaseOption),
        ConditionalConstraint(
          isFieldNotEmpty(ducrPartField) and isFieldEmpty(ducrField) and isFieldEmpty(mucrField),
          "manageChiefConsignment.ducr.error.blank",
          nonEmptyOptionString
        )
      )
    )

    def ducrPartMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldNotEmpty(ducrPartField) and isFieldEmpty(mucrField),
          "manageChiefConsignment.ducrPartId.error",
          isValidDucrPartIdOption
        )
      )
    )

    Forms
      .mapping("mucr" -> mucrMapping, "ducr" -> ducrMapping, "ducrPartId" -> ducrPartMapping)(ChiefConsignment.apply)(chiefConsignmentForm =>
        Option((chiefConsignmentForm.mucr, chiefConsignmentForm.ducr, chiefConsignmentForm.ducrPartId))
      )
      .verifying(
        "manageChiefConsignment.error.blank",
        fields =>
          fields match {
            case ChiefConsignment(None, None, None) => false
            case _                                  => true
          }
      )
      .verifying(
        "manageChiefConsignment.error.mismatchedInput",
        fields =>
          fields match {
            case ChiefConsignment(Some(_), Some(_), Some(_)) => false
            case ChiefConsignment(Some(_), Some(_), _)       => false
            case ChiefConsignment(Some(_), _, Some(_))       => false
            case _                                           => true
          }
      )
  }

  def form(): Form[ChiefConsignment] = Form(mapping)
}
