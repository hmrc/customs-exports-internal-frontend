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

package testdata

import connectors.exchanges.ActionType.MovementType
import connectors.exchanges._
import forms.GoodsDeparted.DepartureLocation.OutOfTheUk
import forms.Transport.ModesOfTransport
import forms._
import forms.common.{Date, Time}
import models.UcrBlock
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import models.submissions.Submission
import testdata.CommonTestData.{conversationId, correctUcr, providerId, validEori}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalTime, ZoneId}

object MovementsTestData {

  private val zoneId: ZoneId = ZoneId.of("Europe/London")
  private val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT
  val movementDetails = new MovementDetails(zoneId)

  def validArrivalAnswers: ArrivalAnswers =
    ArrivalAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr)),
      arrivalDetails = Some(ArrivalDetails(Date(LocalDate.now().minusDays(1)), Time(LocalTime.of(1, 1)))),
      location = Some(Location("GBAUEMAEMAEMA"))
    )

  def validRetrospectiveArrivalAnswers: RetrospectiveArrivalAnswers =
    RetrospectiveArrivalAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr)),
      location = Some(Location("GBAUEMAEMAEMA"))
    )

  def validDepartureAnswers: DepartureAnswers =
    DepartureAnswers(
      eori = Some(validEori),
      consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr)),
      departureDetails = Some(DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0)))),
      location = Some(Location("GBAUEMAEMAEMA")),
      goodsDeparted = Some(GoodsDeparted(OutOfTheUk)),
      transport = Some(Transport(modeOfTransport = Some(ModesOfTransport.Sea), nationality = Some("GB"), transportId = Some("transportID")))
    )

  def validArrivalExchange: ArrivalExchange = ArrivalExchange(
    eori = validEori,
    providerId = providerId,
    consignmentReference = ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr),
    location = Location("GBAUEMAEMAEMA"),
    movementDetails = MovementDetailsExchange(
      dateTimeFormatter.format(ArrivalDetails(Date(LocalDate.now().minusDays(1)), Time(LocalTime.of(1, 1))).goodsArrivalMoment(zoneId))
    )
  )

  def validRetrospectiveArrivalExchange: RetrospectiveArrivalExchange = RetrospectiveArrivalExchange(
    eori = validEori,
    providerId = providerId,
    consignmentReference = ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr),
    location = Location("GBAUEMAEMAEMA")
  )

  def validDepartureExchange: DepartureExchange = DepartureExchange(
    eori = validEori,
    providerId = providerId,
    consignmentReference = ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr),
    location = Location("GBAUEMAEMAEMA"),
    movementDetails = MovementDetailsExchange(
      dateTimeFormatter.format(DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0))).goodsDepartureMoment(zoneId))
    ),
    transport = Transport(modeOfTransport = Some(ModesOfTransport.Sea), nationality = Some("GB"), transportId = Some("transportID"))
  )

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucrBlocks: Seq[UcrBlock] = Seq(UcrBlock(ucr = correctUcr, ucrType = "D")),
    actionType: ActionType = MovementType.Arrival,
    requestTimestamp: Instant = models.now
  ): Submission =
    Submission(eori = eori, conversationId = conversationId, ucrBlocks = ucrBlocks, actionType = actionType, requestTimestamp = requestTimestamp)
}
