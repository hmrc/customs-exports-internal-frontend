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

import java.time.{Instant, ZoneId}

import connectors.exchanges.MovementExchange
import forms.{Choice, ConsignmentReferences, MovementBuilder, MovementDetails}
import models.UcrBlock
import models.cache.{ArrivalAnswers, DepartureAnswers}
import models.submissions.{ActionType, Submission}
import testdata.CommonTestData.{conversationId, correctUcr, providerId, validEori}

object MovementsTestData {

  private val zoneId: ZoneId = ZoneId.of("Europe/London")

  val movementDetails = new MovementDetails(zoneId)

  val movementBuilder = new MovementBuilder(zoneId)

  def validMovementRequest(movementType: Choice): MovementExchange =
    movementType match {
      case Choice.Arrival   => movementBuilder.createMovementRequest(providerId, validArrivalAnswers)
      case Choice.Departure => movementBuilder.createMovementRequest(providerId, validDepartureAnswers)
    }

  def validArrivalAnswers =
    ArrivalAnswers(Some("eori"), consignmentReferences = Some(ConsignmentReferences("ref", "value")))

  def validDepartureAnswers =
    DepartureAnswers(Some("eori"), consignmentReferences = Some(ConsignmentReferences("ref", "value")))

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucrBlocks: Seq[UcrBlock] = Seq(UcrBlock(ucr = correctUcr, ucrType = "D")),
    actionType: ActionType = ActionType.Arrival,
    requestTimestamp: Instant = Instant.now()
  ): Submission =
    Submission(eori = eori, conversationId = conversationId, ucrBlocks = ucrBlocks, actionType = actionType, requestTimestamp = requestTimestamp)
}
