/*
 * Copyright 2019 HM Revenue & Customs
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

package models.cache

import forms.Choice
import play.api.libs.json.{Json, OFormat}

case class MovementCache(
  pid: String,
  choice: Choice,
  arrivalJourney: Option[ArrivalJourney] = None,
  departureJourney: Option[DepartureJourney] = None,
  associateJourney: Option[AssociateJourney] = None,
  dissociateJourney: Option[DissociateJourney] = None,
  shutJourney: Option[ShutJourney] = None
)

object MovementCache {
  implicit val format: OFormat[MovementCache] = Json.format[MovementCache]
}

case class ArrivalJourney(field: Option[String] = None)

object ArrivalJourney {
  implicit val format: OFormat[ArrivalJourney] = Json.format[ArrivalJourney]
}

case class DepartureJourney(field: Option[String] = None)

object DepartureJourney {
  implicit val format: OFormat[DepartureJourney] = Json.format[DepartureJourney]
}

case class AssociateJourney(field: Option[String] = None)

object AssociateJourney {
  implicit val format: OFormat[AssociateJourney] = Json.format[AssociateJourney]
}

case class DissociateJourney(field: Option[String] = None)

object DissociateJourney {
  implicit val format: OFormat[DissociateJourney] = Json.format[DissociateJourney]
}

case class ShutJourney(field: Option[String] = None)

object ShutJourney {
  implicit val format: OFormat[ShutJourney] = Json.format[ShutJourney]
}