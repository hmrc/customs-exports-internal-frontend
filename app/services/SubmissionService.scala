/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.exchanges._
import forms._
import metrics.MovementsMetrics
import models.cache.JourneyType.JourneyType
import models.cache._
import models.{ReturnToStartException, UcrType}
import repositories.CacheRepository
import services.audit.{AuditService, AuditType}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject() (
  cacheRepository: CacheRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics,
  movementBuilder: MovementBuilder
)(implicit ec: ExecutionContext) {

  private val success = "Success"
  private val failed = "Failed"

  def submit(providerId: String, answers: DisassociateUcrAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val disUcr = answers.ucr.getOrElse(throw ReturnToStartException)
    val exchange = disUcr.kind match {
      case UcrType.Ducr     => DisassociateDUCRExchange(providerId, eori, disUcr.ucr)
      case UcrType.DucrPart => DisassociateDUCRPartExchange(providerId, eori, disUcr.ucr)
      case UcrType.Mucr     => DisassociateMUCRExchange(providerId, eori, disUcr.ucr)
    }

    connector
      .submit(exchange)
      .andThen {
        case Success(_) =>
          cacheRepository.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditDisassociate(providerId, disUcr.ucr, success)
          }
        case Failure(_) =>
          auditService.auditDisassociate(providerId, disUcr.ucr, failed)
      }
  }

  def submit(providerId: String, answers: AssociateUcrAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val mucr = answers.parentMucr.map(_.mucr).getOrElse(throw ReturnToStartException)
    val assUcr = answers.childUcr.getOrElse(throw ReturnToStartException)
    val exchange = assUcr.kind match {
      case UcrType.Ducr     => AssociateDUCRExchange(providerId, eori, mucr, assUcr.ucr)
      case UcrType.DucrPart => AssociateDUCRPartExchange(providerId, eori, mucr, assUcr.ucr)
      case UcrType.Mucr     => AssociateMUCRExchange(providerId, eori, mucr, assUcr.ucr)
    }

    connector
      .submit(exchange)
      .andThen {
        case Success(_) =>
          cacheRepository.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditAssociate(providerId, mucr, assUcr.ucr, success)
          }
        case Failure(_) =>
          auditService.auditAssociate(providerId, mucr, assUcr.ucr, failed)
      }
  }

  def submit(providerId: String, answers: ShutMucrAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    val eori = answers.eori.getOrElse(throw ReturnToStartException)
    val mucr = answers.shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)

    connector
      .submit(ShutMUCRExchange(providerId, eori, mucr))
      .andThen {
        case Success(_) =>
          cacheRepository.removeByProviderId(providerId).flatMap { _ =>
            auditService.auditShutMucr(providerId, mucr, success)
          }
        case Failure(_) =>
          auditService.auditShutMucr(providerId, mucr, failed)
      }
  }

  def submit(providerId: String, answers: MovementAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    val journeyType = answers.`type`
    val data = movementBuilder.createMovementExchange(providerId, answers)
    val timer = metrics.startTimer(journeyType)

    auditService.auditAllPagesUserInput(providerId, answers)

    (for {
      conversationId <- connector.submit(data)
      _ <- cacheRepository.removeByProviderId(providerId)
    } yield conversationId).andThen {
      case Success(_) => auditService.auditMovements(data, success, movementAuditType(journeyType))
      case Failure(_) => auditService.auditMovements(data, failed, movementAuditType(journeyType))
    }.andThen { case _ =>
      metrics.incrementCounter(journeyType)
      timer.stop()
    }
  }

  private def movementAuditType(journeyType: JourneyType): AuditType.Value = journeyType match {
    case JourneyType.ARRIVE               => AuditType.AuditArrival
    case JourneyType.RETROSPECTIVE_ARRIVE => AuditType.AuditRetrospectiveArrival
    case JourneyType.DEPART               => AuditType.AuditDeparture
  }
}
