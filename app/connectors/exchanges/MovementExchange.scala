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

package connectors.exchanges

import connectors.exchanges.ActionType.MovementType
import forms._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.play.json.Union

trait MovementExchange {
  val eori: String
  val providerId: String
  val choice: MovementType
  val consignmentReference: ConsignmentReferences
  val location: Location
}

object MovementExchange {
  implicit val format: Format[MovementExchange] = Union
    .from[MovementExchange]("choice")
    .and[ArrivalExchange](MovementType.Arrival.toString)
    .and[RetrospectiveArrivalExchange](MovementType.RetrospectiveArrival.toString)
    .and[DepartureExchange](MovementType.Departure.toString)
    .format
}

case class ArrivalExchange(
  override val eori: String,
  override val providerId: String,
  override val consignmentReference: ConsignmentReferences,
  override val location: Location,
  movementDetails: MovementDetailsExchange
) extends MovementExchange {
  override val choice: MovementType = MovementType.Arrival
}
object ArrivalExchange {
  implicit val format: OFormat[ArrivalExchange] = Json.format[ArrivalExchange]
}

case class RetrospectiveArrivalExchange(
  override val eori: String,
  override val providerId: String,
  override val consignmentReference: ConsignmentReferences,
  override val location: Location
) extends MovementExchange {
  override val choice: MovementType = MovementType.RetrospectiveArrival
}
object RetrospectiveArrivalExchange {
  implicit val format: OFormat[RetrospectiveArrivalExchange] = Json.format[RetrospectiveArrivalExchange]
}

case class DepartureExchange(
  override val eori: String,
  override val providerId: String,
  override val consignmentReference: ConsignmentReferences,
  override val location: Location,
  movementDetails: MovementDetailsExchange,
  transport: Transport
) extends MovementExchange {
  override val choice: MovementType = MovementType.Departure
}
object DepartureExchange {
  implicit val format: OFormat[DepartureExchange] = Json.format[DepartureExchange]
}
