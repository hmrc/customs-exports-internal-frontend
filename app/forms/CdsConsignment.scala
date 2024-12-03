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

import forms.CdsConsignment._
import models.{UcrBlock, UcrType}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.Condition
import utils.FieldValidator._

case class CdsConsignment(mucr: Option[String], ducr: Option[String], ducrPartId: Option[String]) {

  def toUcrBlock: UcrBlock = this match {
    case CdsConsignment(Some(mucr), None, None) => UcrBlock(ucr = mucr, ucrType = UcrType.Mucr.codeValue, chiefUcr = Some(true))
    case CdsConsignment(None, Some(ducr), None) => UcrBlock(ucr = ducr, ucrType = UcrType.Ducr.codeValue, chiefUcr = Some(true))
    case CdsConsignment(None, Some(ducr), Some(ducrPartId)) =>
      UcrBlock(ucr = s"$ducr$Separator$ducrPartId", ucrType = UcrType.DucrPart.codeValue, chiefUcr = Some(true))
    case _ => throw new IllegalArgumentException(s"Cannot create valid UcrBlock instance from CdsConsignment: [${this.toString}]")
  }
}

object CdsConsignment {
  implicit val format: OFormat[CdsConsignment] = Json.format[CdsConsignment]

  val Separator = "-"

  def apply(ucrBlock: UcrBlock): CdsConsignment =
    ucrBlock.ucrType match {
      case UcrType.DucrPart.codeValue =>
        val separatorIndex = ucrBlock.ucr.lastIndexOf(Separator)
        val (ducr, ducrPartId) = ucrBlock.ucr.splitAt(separatorIndex)
        val ducrPartIdWithoutSeparator = ducrPartId.tail

        CdsConsignment(mucr = None, ducr = Some(ducr), ducrPartId = Some(ducrPartIdWithoutSeparator))
      case UcrType.Ducr.codeValue => CdsConsignment(mucr = None, ducr = Some(ucrBlock.ucr), ducrPartId = None)
      case UcrType.Mucr.codeValue => CdsConsignment(mucr = Some(ucrBlock.ucr), ducr = None, ducrPartId = None)
      case _ => throw new IllegalArgumentException(s"Cannot create CdsConsignment instance from UcrBlock of type: [${ucrBlock.ucrType}]")
    }

  val mapping: Mapping[CdsConsignment] = {
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
          "manageCdsConsignment.mucr.error",
          validMucrIgnoreCaseOption
        )
      )
    )

    def ducrMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(isFieldNotEmpty(ducrField) and isFieldEmpty(mucrField), "manageCdsConsignment.ducr.error", validDucrIgnoreCaseOption),
        ConditionalConstraint(
          isFieldNotEmpty(ducrPartField) and isFieldEmpty(ducrField) and isFieldEmpty(mucrField),
          "manageCdsConsignment.ducr.error.blank",
          nonEmptyOptionString
        )
      )
    )

    def ducrPartMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldNotEmpty(ducrPartField) and isFieldEmpty(mucrField),
          "manageCdsConsignment.ducrPartId.error",
          isValidDucrPartIdOption
        )
      )
    )

    Forms
      .mapping("mucr" -> mucrMapping, "ducr" -> ducrMapping, "ducrPartId" -> ducrPartMapping)(CdsConsignment.apply)(CdsConsignment.unapply)
      .verifying(
        "manageCdsConsignment.error.blank",
        fields =>
          fields match {
            case CdsConsignment(None, None, None) => false
            case _                                  => true
          }
      )
      .verifying(
        "manageCdsConsignment.error.mismatchedInput",
        fields =>
          fields match {
            case CdsConsignment(Some(_), Some(_), Some(_)) => false
            case CdsConsignment(Some(_), Some(_), _)       => false
            case CdsConsignment(Some(_), _, Some(_))       => false
            case _                                           => true
          }
      )
  }

  def form(): Form[CdsConsignment] = Form(mapping)
}