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

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import connectors.exchanges._
import javax.inject.Inject
import models.ReturnToStartException
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers, RetrospectiveArrivalAnswers}

class MovementBuilder @Inject()(zoneId: ZoneId) {

  private val movementDateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def createMovementExchange(providerId: String, answers: MovementAnswers): MovementExchange = answers match {
    case arrivalAnswers: ArrivalAnswers                   => createMovementArrivalRequest(providerId, arrivalAnswers)
    case retroArrivalAnswers: RetrospectiveArrivalAnswers => createRetrospectiveArrivalExchange(providerId, retroArrivalAnswers)
    case departureAnswers: DepartureAnswers               => createMovementDepartureRequest(providerId, departureAnswers)
  }

  private def createMovementArrivalRequest(providerId: String, answers: ArrivalAnswers) =
    ArrivalExchange(
      eori = answers.eori.getOrElse(throw ReturnToStartException),
      providerId = providerId,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers).getOrElse(throw ReturnToStartException),
      location = answers.location.getOrElse(throw ReturnToStartException),
      arrivalReference = answers.arrivalReference.getOrElse(throw ReturnToStartException)
    )

  private def createRetrospectiveArrivalExchange(providerId: String, answers: RetrospectiveArrivalAnswers) =
    RetrospectiveArrivalExchange(
      eori = answers.eori.getOrElse(throw ReturnToStartException),
      providerId = providerId,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      location = answers.location.getOrElse(throw ReturnToStartException)
    )

  private def createMovementDepartureRequest(providerId: String, answers: DepartureAnswers) =
    DepartureExchange(
      eori = answers.eori.getOrElse(throw ReturnToStartException),
      providerId = providerId,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers).getOrElse(throw ReturnToStartException),
      location = answers.location.getOrElse(throw ReturnToStartException),
      transport = answers.transport.getOrElse(throw ReturnToStartException)
    )

  private def movementDetails(answers: ArrivalAnswers): Option[MovementDetailsExchange] =
    answers.arrivalDetails
      .map(arrival => movementDateTimeFormatter.format(arrival.goodsArrivalMoment(zoneId)))
      .map(MovementDetailsExchange(_))

  private def movementDetails(answers: DepartureAnswers): Option[MovementDetailsExchange] =
    answers.departureDetails
      .map(departure => movementDateTimeFormatter.format(departure.goodsDepartureMoment(zoneId)))
      .map(MovementDetailsExchange(_))

}
