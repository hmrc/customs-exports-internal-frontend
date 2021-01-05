/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.GoodsDeparted.DepartureLocation
import forms.GoodsDeparted.DepartureLocation.{BackIntoTheUk, OutOfTheUk}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._

case class GoodsDeparted(departureLocation: DepartureLocation)

object GoodsDeparted {

  val formId = "GoodsDeparted"

  private val allowedLocationsValues = Set(OutOfTheUk.value, BackIntoTheUk.value)

  def form2Model(input: String): GoodsDeparted = GoodsDeparted(DepartureLocation(input))
  def model2Form(goodsDeparted: GoodsDeparted): Option[String] = Some(goodsDeparted.departureLocation.value)

  val mapping: Mapping[GoodsDeparted] =
    Forms.mapping(
      "departureLocation" -> requiredRadio("goodsDeparted.departureLocation.error.empty")
        .verifying("goodsDeparted.departureLocation.error.incorrect", input => allowedLocationsValues(input))
    )(form2Model)(model2Form)

  def form: Form[GoodsDeparted] = Form(mapping)

  sealed abstract class DepartureLocation(val value: String)
  object DepartureLocation {
    case object OutOfTheUk extends DepartureLocation("outOfTheUk")
    case object BackIntoTheUk extends DepartureLocation("backIntoTheUk")

    def apply(input: String): DepartureLocation = input match {
      case OutOfTheUk.value    => OutOfTheUk
      case BackIntoTheUk.value => BackIntoTheUk
    }

    implicit object DepartureLocationFormat extends Format[DepartureLocation] {
      override def reads(json: JsValue): JsResult[DepartureLocation] = json match {
        case JsString(departureLocationValue) => JsSuccess(DepartureLocation(departureLocationValue))
        case _                                => JsError("Incorrect DepartureLocation")
      }

      override def writes(o: DepartureLocation): JsValue = JsString(o.value)
    }
  }

  implicit val format = Json.format[GoodsDeparted]
}
