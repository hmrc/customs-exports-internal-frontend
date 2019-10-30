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
import connectors.exchanges.{AssociateUCRRequest, DisassociateDUCRRequest}
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache.{Answers, AssociateUcrAnswers, Cache, DisassociateUcrAnswers, JourneyType}
import play.api.http.Status
import repositories.MovementRepository
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject()(
  movementRepository: MovementRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics
)(implicit ec: ExecutionContext) {

  def submit(pid: String, answers: DisassociateUcrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val ucr = answers.ucr.getOrElse(throw ReturnToStartException)

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

  def submit(pid: String, answers: AssociateUcrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val mucr = answers.mucrOptions.map(_.mucr).getOrElse(throw ReturnToStartException)
    val ucr = answers.associateUcr.map(_.ucr).getOrElse(throw ReturnToStartException)

    connector
      .submit(AssociateUCRRequest(pid, eori, mucr, ucr))
      .flatMap(_ => movementRepository.removeByPid(pid))
      .andThen {
        case Success(_) =>
          auditService.auditAssociate(eori, mucr, ucr, "Success")
        case Failure(_) =>
          auditService.auditAssociate(eori, mucr, ucr, "Failed")
      }
  }

  def submitMovementRequest(pid: String, answers: Answers)(implicit hc: HeaderCarrier): Future[ConsignmentReferences] = {
    val cache = Cache(pid, answers)

    val data = Movement.createMovementRequest(pid, answers)
    val timer = metrics.startTimer(cache.answers.`type`)

    auditService.auditAllPagesUserInput(answers)

    val movementAuditType =
      if (cache.answers.`type` == JourneyType.ARRIVE) AuditTypes.AuditArrival else AuditTypes.AuditDeparture

    connector.submit(data).map { _ =>
      metrics.incrementCounter(cache.answers.`type`)
      auditService
        .auditMovements(data, Status.OK.toString, movementAuditType)
      timer.stop()
      data.consignmentReference
    }
  }
}
