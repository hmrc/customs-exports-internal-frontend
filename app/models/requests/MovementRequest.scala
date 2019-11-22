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

package models.requests

import forms._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.play.json.Union

case class ArrivalRequest(
  override val eori: String,
  override val providerId: String,
  override val consignmentReference: ConsignmentReferences,
  override val movementDetails: MovementDetailsRequest,
  override val location: Location,
  arrivalReference: ArrivalReference
) extends MovementRequest {
  override val choice: MovementType = MovementType.Arrival
}
object ArrivalRequest {
  implicit val format: OFormat[ArrivalRequest] = Json.format[ArrivalRequest]
}

case class DepartureRequest(
  override val eori: String,
  override val providerId: String,
  override val consignmentReference: ConsignmentReferences,
  override val movementDetails: MovementDetailsRequest,
  override val location: Location,
  transport: Transport
) extends MovementRequest {
  override val choice: MovementType = MovementType.Departure
}
object DepartureRequest {
  implicit val format: OFormat[DepartureRequest] = Json.format[DepartureRequest]
}

trait MovementRequest {
  val eori: String
  val providerId: String
  val choice: MovementType
  val consignmentReference: ConsignmentReferences
  val movementDetails: MovementDetailsRequest
  val location: Location
}

object MovementRequest {
  implicit val format: Format[MovementRequest] = Union
    .from[MovementRequest]("choice")
    .and[DepartureRequest](MovementType.Departure.toString)
    .and[ArrivalRequest](MovementType.Arrival.toString)
    .format
}
