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

package controllers

import config.ErrorHandler
import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import javax.inject.Inject
import models.cache.{ArrivalAnswers, JourneyType}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.movement_confirmation_page
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  errorHandler: ErrorHandler,
  movementRepository: MovementRepository,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  arrivalSummaryPage: arrival_summary_page,
  departureSummaryPage: departure_summary_page,
  movementConfirmationPage: movement_confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass)

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val answers = request.answersAs[ArrivalAnswers]
    answers.`type` match {
      case JourneyType.ARRIVE => Ok(arrivalSummaryPage(answers))
      case JourneyType.DEPART => Ok(departureSummaryPage(answers))
    }
  }

  def submitMovementRequest(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    submissionService
      .submitMovementRequest(request.pid, request.answersAs[ArrivalAnswers])
      .flatMap {
        case (Some(consignmentReferences), ACCEPTED) =>
          movementRepository.delete(request.pid).map { _ =>
            Ok(movementConfirmationPage(consignmentReferences))
          }
        case _ =>
          Future.successful {
            logger.warn(s"No movement data found in cache.")
            errorHandler.getInternalServerErrorPage
          }
      }
  }
}
