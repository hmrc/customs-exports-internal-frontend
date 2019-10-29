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

package services

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.DisassociateDUCRRequest
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache.{Cache, DisassociateUcrAnswers, JourneyType}
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import repositories.MovementRepository
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject()(movementRepository: MovementRepository,
                                  connector: CustomsDeclareExportsMovementsConnector,
                                  auditService: AuditService,
                                  metrics: MovementsMetrics
                                 )(implicit ec: ExecutionContext) {

  def submit(pid: String, answers: DisassociateUcrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val ucr = answers.ucr.getOrElse(throw ReturnToStartException)

    auditService.auditAllPagesUserInput(answers)
    connector
      .submit(DisassociateDUCRRequest(pid, eori, ucr))
      .flatMap(_ => movementRepository.removeByPid(pid))
      .andThen {
        case Success(_) =>
          auditService.auditDisassociate(eori, ucr, "Success")
        case Failure(_) =>
          auditService.auditDisassociate(eori, ucr, "Failed")
      }
  }

  def submitMovementRequest(pid: String)(implicit hc: HeaderCarrier): Future[(Option[ConsignmentReferences], Int)] =
    movementRepository.findByPid(pid).flatMap {
      case Some(cache) =>
        val data = createMovementRequest(cache)
        val timer = metrics.startTimer(cache.answers.`type`)

        auditService.auditAllPagesUserInput(cache.answers)

        val movementAuditType =
          if (cache.answers.`type` == JourneyType.ARRIVE) AuditTypes.AuditArrival else AuditTypes.AuditDeparture

        sendMovementRequest(data).map { _ =>
          metrics.incrementCounter(cache.answers.`type`)
          auditService
            .auditMovements(data, "200", movementAuditType)
          timer.stop()
          (Some(data.consignmentReference), 200)
        }
      case _ =>
        Future.successful((None, INTERNAL_SERVER_ERROR))
    }

  private def sendMovementRequest(movementRequest: MovementRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    movementRequest.choice match {
      case MovementType.Arrival => connector.sendArrivalDeclaration(movementRequest)
      case MovementType.Departure => connector.sendDepartureDeclaration(movementRequest)
    }

  private def createMovementRequest(cache: Cache): MovementRequest =
  // TODO - implement
    MovementRequest(
      eori = "TODO",
      choice = MovementType.Arrival,
      consignmentReference = ConsignmentReferences("todo", "todo"),
      movementDetails = MovementDetailsRequest("todo"),
      location = None,
      transport = None,
      arrivalReference = None
    )

}
