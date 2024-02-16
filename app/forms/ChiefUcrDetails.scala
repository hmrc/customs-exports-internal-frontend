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

import forms.ChiefUcrDetails._
import models.{UcrBlock, UcrType}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.Condition
import utils.FieldValidator._

case class ChiefUcrDetails(mucr: Option[String], ducr: Option[String], ducrPartId: Option[String]) {

  def toUcrBlock: UcrBlock = this match {
    case ChiefUcrDetails(Some(mucr), None, None)             => UcrBlock(ucr = mucr, ucrType = UcrType.Mucr.codeValue)
    case ChiefUcrDetails(None, Some(ducr), None)             => UcrBlock(ucr = ducr, ucrType = UcrType.Ducr.codeValue)
    case ChiefUcrDetails(None, Some(ducr), Some(ducrPartId)) => UcrBlock(ucr = s"$ducr$Separator$ducrPartId", ucrType = UcrType.DucrPart.codeValue)
    case _ => throw new IllegalArgumentException(s"Cannot create valid UcrBlock instance from ChiefUcrDetails: [${this.toString}]")
  }
}

object ChiefUcrDetails {
  implicit val format: OFormat[ChiefUcrDetails] = Json.format[ChiefUcrDetails]

  val Separator = "-"

  def apply(ucrBlock: UcrBlock): ChiefUcrDetails =
    ucrBlock.ucrType match {
      case UcrType.DucrPart.codeValue =>
        val separatorIndex = ucrBlock.ucr.lastIndexOf(Separator)
        val (ducr, ducrPartId) = ucrBlock.ucr.splitAt(separatorIndex)
        val ducrPartIdWithoutSeparator = ducrPartId.tail

        ChiefUcrDetails(mucr = None, ducr = Some(ducr), ducrPartId = Some(ducrPartIdWithoutSeparator))
      case UcrType.Ducr.codeValue => ChiefUcrDetails(mucr = None, ducr = Some(ucrBlock.ucr), ducrPartId = None)
      case UcrType.Mucr.codeValue => ChiefUcrDetails(mucr = Some(ucrBlock.ucr), ducr = None, ducrPartId = None)
      case _ => throw new IllegalArgumentException(s"Cannot create DucrPartDetails instance from UcrBlock of type: [${ucrBlock.ucrType}]")
    }

  val mapping: Mapping[ChiefUcrDetails] = {
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
          "ducrPartDetails.mucr.error",
          validMucrIgnoreCaseOption
        )
      )
    )

    def ducrMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(isFieldNotEmpty(ducrField) and isFieldEmpty(mucrField), "ducrPartDetails.ducr.error", validDucrIgnoreCaseOption),
        ConditionalConstraint(
          isFieldNotEmpty(ducrPartField) and isFieldEmpty(ducrField) and isFieldEmpty(mucrField),
          "ducrPartDetails.ducr.error.blank",
          nonEmptyOptionString
        )
      )
    )

    def ducrPartMapping: AdditionalConstraintsMapping[Option[String]] = AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.trim.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(isFieldNotEmpty(ducrPartField) and isFieldEmpty(mucrField), "ducrPartDetails.ducrPartId.error", isValidDucrPartIdOption)
      )
    )

    Forms
      .mapping("mucr" -> mucrMapping, "ducr" -> ducrMapping, "ducrPartId" -> ducrPartMapping)(ChiefUcrDetails.apply)(ChiefUcrDetails.unapply)
      .verifying(
        "ducrPartDetails.error.blank",
        fields =>
          fields match {
            case ChiefUcrDetails(None, None, None) => false
            case _                                 => true
          }
      )
      .verifying(
        "ducrPartDetails.error.mismatchedInput",
        fields =>
          fields match {
            case ChiefUcrDetails(Some(_), Some(_), Some(_)) => false
            case ChiefUcrDetails(Some(_), Some(_), _)       => false
            case ChiefUcrDetails(Some(_), _, Some(_))       => false
            case _                                          => true
          }
      )
  }

  def form(): Form[ChiefUcrDetails] = Form(mapping)
}
