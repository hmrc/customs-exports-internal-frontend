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
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.cache.JourneyType
import models.requests.{MovementRequest, MovementType}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import repositories.MovementRepository
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  movementRepository: MovementRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics
)(implicit ec: ExecutionContext) {

  def submitMovementRequest(pid: String)(implicit hc: HeaderCarrier): Future[(Option[ConsignmentReferences], Int)] =
    movementRepository.findByPid(pid).flatMap {
      case Some(cache) =>
        val data = Movement.createMovementRequest(cache)
        val timer = metrics.startTimer(cache.answers.`type`)

        auditService.auditAllPagesUserInput(cache.answers)

        val movementAuditType =
          if (cache.answers.`type` == JourneyType.ARRIVE) AuditTypes.AuditArrival else AuditTypes.AuditDeparture

        sendMovementRequest(data).map { submitResponse =>
          metrics.incrementCounter(cache.answers.`type`)
          auditService
            .auditMovements(data, submitResponse.status.toString, movementAuditType)
          timer.stop()
          (Some(data.consignmentReference), submitResponse.status)
        }
      case _ =>
        Future.successful((None, INTERNAL_SERVER_ERROR))
    }

  private def sendMovementRequest(movementRequest: MovementRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    movementRequest.choice match {
      case MovementType.Arrival   => connector.sendArrivalDeclaration(movementRequest)
      case MovementType.Departure => connector.sendDepartureDeclaration(movementRequest)
    }

}
