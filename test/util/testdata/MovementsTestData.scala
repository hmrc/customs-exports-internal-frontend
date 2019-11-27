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

package testdata

import java.time.{Instant, LocalDate, LocalTime, ZoneId}

import connectors.exchanges.MovementExchange
import forms.GoodsDeparted.DepartureLocation.OutOfTheUk
import forms.Transport.ModesOfTransport
import forms._
import forms.common.{Date, Time}
import models.UcrBlock
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import models.submissions.{ActionType, Submission}
import testdata.CommonTestData.{conversationId, correctUcr, providerId, validEori}

object MovementsTestData {

  private val zoneId: ZoneId = ZoneId.of("Europe/London")

  val movementDetails = new MovementDetails(zoneId)

  val movementBuilder = new MovementBuilder(zoneId)

  def validMovementRequest(movementType: Choice): MovementExchange =
    movementType match {
      case Choice.Arrival   => movementBuilder.createMovementExchange(providerId, validArrivalAnswers)
      case Choice.Departure => movementBuilder.createMovementExchange(providerId, validDepartureAnswers)
    }

  def validArrivalAnswers =
    ArrivalAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences("D", correctUcr)),
      arrivalReference = Some(ArrivalReference(Some("arrivalReference"))),
      arrivalDetails = Some(ArrivalDetails(Date(LocalDate.now().minusDays(1)), Time(LocalTime.of(1, 1)))),
      location = Some(Location("GBAUEMAEMAEMA"))
    )

  def validRetrospectiveArrivalAnswers =
    RetrospectiveArrivalAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences("D", correctUcr)),
      location = Some(Location("GBAUEMAEMAEMA"))
    )

  def validDepartureAnswers =
    DepartureAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences("D", correctUcr)),
      departureDetails = Some(DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0)))),
      location = Some(Location("GBAUEMAEMAEMA")),
      goodsDeparted = Some(GoodsDeparted(OutOfTheUk)),
      transport = Some(Transport(modeOfTransport = Some(ModesOfTransport.Sea), nationality = Some("GB"), transportId = Some("transportID")))
    )

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucrBlocks: Seq[UcrBlock] = Seq(UcrBlock(ucr = correctUcr, ucrType = "D")),
    actionType: ActionType = ActionType.Arrival,
    requestTimestamp: Instant = Instant.now()
  ): Submission =
    Submission(eori = eori, conversationId = conversationId, ucrBlocks = ucrBlocks, actionType = actionType, requestTimestamp = requestTimestamp)
}
