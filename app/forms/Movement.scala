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

package forms

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import models.ReturnToStartException
import models.cache.JourneyType.JourneyType
import models.cache.{JourneyType, MovementAnswers}
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}

object Movement {

  private val departureDateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def createMovementRequest(pid: String, answers: MovementAnswers): MovementRequest =
    MovementRequest(
      eori = answers.eori.getOrElse(throw ReturnToStartException),
      providerId = Some(pid),
      choice = movementType(answers.`type`),
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers),
      location = answers.location,
      arrivalReference = answers.arrivalReference,
      transport = answers.transport
    )

  private def movementType(journeyType: JourneyType) = journeyType match {
    case JourneyType.ARRIVE => MovementType.Arrival
    case JourneyType.DEPART => MovementType.Departure
  }

  private def movementDetails(answers: MovementAnswers) =
    answers.`type` match {
      case JourneyType.ARRIVE =>
        MovementDetailsRequest(
          answers.arrivalDetails
            .map(arrivalDetails => s"${arrivalDetails.dateOfArrival.toString}T${arrivalDetails.timeOfArrival.toString}:00")
            .getOrElse("")
        )
      case JourneyType.DEPART =>
        MovementDetailsRequest(
          answers.departureDetails
            .map(departureDetails => departureDateTimeFormatter.format(departureDetails.goodsDepartureMoment.atZone(ZoneId.systemDefault())))
            .getOrElse("")
        )
    }
}
