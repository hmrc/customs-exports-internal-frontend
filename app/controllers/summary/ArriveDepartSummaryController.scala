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

package controllers.summary

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.summary.routes.MovementConfirmationController
import models.cache.JourneyType.{ARRIVE, DEPART, RETROSPECTIVE_ARRIVE}
import models.cache._
import models.summary.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.{arrival_summary_page, departure_summary_page, retrospective_arrival_summary_page}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ArriveDepartSummaryController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  arrivalSummaryPage: arrival_summary_page,
  retrospectiveArrivalSummaryPage: retrospective_arrival_summary_page,
  departureSummaryPage: departure_summary_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val actionValidation = authenticate andThen getJourney(ARRIVE, RETROSPECTIVE_ARRIVE, DEPART)

  val displayPage: Action[AnyContent] = actionValidation { implicit request =>
    request.answers match {
      case arrivalAnswers: ArrivalAnswers                   => Ok(arrivalSummaryPage(arrivalAnswers))
      case retroArrivalAnswers: RetrospectiveArrivalAnswers => Ok(retrospectiveArrivalSummaryPage(retroArrivalAnswers))
      case departureAnswers: DepartureAnswers               => Ok(departureSummaryPage(departureAnswers))
      case _                                                => throw new IllegalArgumentException("Invalid answers type")
    }
  }

  val submitMovementRequest: Action[AnyContent] = actionValidation.async { implicit request =>
    val answers = request.answersAs[MovementAnswers]
    val ucrType = answers.consignmentReferences.map(_.reference.toString)
    val ucr = answers.consignmentReferences.map(_.referenceValue)

    submissionService.submit(request.providerId, request.answersAs[MovementAnswers]).map { conversationId =>
      val values = List(
        Some(JOURNEY_TYPE -> answers.`type`.toString),
        ucr.map(ucr => UCR -> ucr),
        ucrType.map(ucrType => UCR_TYPE -> ucrType),
        Some(CONVERSATION_ID -> conversationId)
      ).flatten

      Redirect(MovementConfirmationController.displayPage).addingToSession(values: _*)
    }
  }
}
