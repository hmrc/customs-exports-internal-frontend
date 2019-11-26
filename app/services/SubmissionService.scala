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
import connectors.exchanges.{AssociateUCRExchange, DisassociateDUCRExchange, ShutMUCRExchange}
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache._
import play.api.http.Status
import repositories.CacheRepository
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject()(
  cache: CacheRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics,
  movementBuilder: MovementBuilder
)(implicit ec: ExecutionContext) {

  def submit(providerId: String, answers: DisassociateUcrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val ucr = answers.ucr.getOrElse(throw ReturnToStartException).ucr

    connector
      .submit(DisassociateDUCRExchange(providerId, eori, ucr))
      .andThen {
        case Success(_) =>
          cache.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditDisassociate(providerId, ucr, "Success")
          }
        case Failure(_) =>
          auditService.auditDisassociate(providerId, ucr, "Failed")
      }
  }

  def submit(providerId: String, answers: AssociateUcrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val mucr = answers.mucrOptions.map(_.mucr).getOrElse(throw ReturnToStartException)
    val ucr = answers.associateUcr.map(_.ucr).getOrElse(throw ReturnToStartException)

    connector
      .submit(AssociateUCRExchange(providerId, eori, mucr, ucr))
      .andThen {
        case Success(_) =>
          cache.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditAssociate(providerId, mucr, ucr, "Success")
          }
        case Failure(_) =>
          auditService.auditAssociate(providerId, mucr, ucr, "Failed")
      }
  }

  def submit(providerId: String, answers: ShutMucrAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val mucr = answers.shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)

    connector
      .submit(ShutMUCRExchange(providerId, eori, mucr))
      .andThen {
        case Success(_) =>
          cache.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditShutMucr(providerId, mucr, "Success")
          }
        case Failure(_) =>
          auditService.auditShutMucr(providerId, mucr, "Failed")
      }
  }

  def submit(providerId: String, answers: MovementAnswers)(implicit hc: HeaderCarrier): Future[ConsignmentReferences] = {
    val cache = Cache(providerId, answers)

    val data = movementBuilder.createMovementRequest(providerId, answers)
    val timer = metrics.startTimer(cache.answers.`type`)

    auditService.auditAllPagesUserInput(answers)

    val movementAuditType =
      if (cache.answers.`type` == JourneyType.ARRIVE) AuditTypes.AuditArrival else AuditTypes.AuditDeparture

    connector.submit(data).map { _ =>
      metrics.incrementCounter(cache.answers.`type`)
      auditService
        .auditMovements(data, "Success", movementAuditType)
      timer.stop()
      data.consignmentReference
    }
  }
}
