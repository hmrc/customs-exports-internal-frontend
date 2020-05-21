/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.EnhancedMapping.requiredRadio
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.FieldValidator._

case class Transport(modeOfTransport: Option[String], nationality: Option[String], transportId: Option[String])

object Transport {
  implicit val format = Json.format[Transport]

  val formId = "Transport"

  object ModesOfTransport {
    val Sea = "1"
    val Rail = "2"
    val Road = "3"
    val Air = "4"
    val PostalOrMail = "5"
    val FixedInstallations = "6"
    val InlandWaterway = "7"
    val Other = "8"
  }

  import ModesOfTransport._

  private def form2OutOfUkModel(modeOfTransport: String, nationality: String, transportId: String): Transport =
    Transport(modeOfTransport = Some(modeOfTransport), nationality = Some(nationality.toUpperCase), transportId = Some(transportId))

  private def model2Form(transport: Transport): Option[(String, String, String)] =
    for {
      modeOfTransport <- transport.modeOfTransport
      nationality <- transport.nationality
      transportId <- transport.transportId
    } yield (modeOfTransport, nationality, transportId)

  val allowedModesOfTransport: Set[String] =
    Set(Sea, Rail, Road, Air, PostalOrMail, FixedInstallations, InlandWaterway, Other)

  private val outOfTheUkMapping = Forms
    .mapping(
      "modeOfTransport" -> requiredRadio("transport.modeOfTransport.empty")
        .verifying("transport.modeOfTransport.error", isContainedIn(allowedModesOfTransport)),
      "nationality" -> text()
        .verifying("transport.nationality.empty", nonEmpty)
        .verifying("transport.nationality.error", isEmpty or (input => isValidCountryCode(input.toUpperCase))),
      "transportId" -> text()
        .verifying("transport.transportId.empty", nonEmpty)
        .verifying("transport.transportId.error", isEmpty or (noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters))
    )(form2OutOfUkModel)(model2Form)

  private val atLeastOneIsEmpty: Transport => Boolean = (t: Transport) => t.transportId.isEmpty || t.nationality.isEmpty || t.modeOfTransport.isEmpty

  private val backIntoTheUkMapping = Forms
    .mapping(
      "modeOfTransport" -> optional(text().verifying("transport.modeOfTransport.error", isContainedIn(allowedModesOfTransport))),
      "nationality" -> optional(text().verifying("transport.nationality.error", isEmpty or (input => isValidCountryCode(input.toUpperCase)))),
      "transportId" -> optional(
        text().verifying("transport.transportId.error", isEmpty or (noLongerThan(35) and isAlphanumericWithAllowedSpecialCharacters))
      )
    )(form2IntoUkModel)(Transport.unapply)
    .verifying("transport.backIntoTheUk.error.allFieldsEntered", atLeastOneIsEmpty)

  private def form2IntoUkModel(modeOfTransport: Option[String], nationality: Option[String], transportId: Option[String]): Transport =
    new Transport(modeOfTransport, nationality.map(_.toUpperCase), transportId)

  def outOfTheUkForm: Form[Transport] = Form(outOfTheUkMapping)
  def backIntoTheUkForm: Form[Transport] = Form(backIntoTheUkMapping)
}
