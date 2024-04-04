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
import forms.ConsignmentReferenceType
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import models.summary.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.shut_mucr_summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ShutMucrSummaryController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  summaryPage: shut_mucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val mucr = request.answersAs[ShutMucrAnswers].shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)
    Ok(summaryPage(mucr))
  }

  val submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    val answers = request.answersAs[ShutMucrAnswers]
    val ucrType = Some(ConsignmentReferenceType.M.toString)
    val ucr = answers.shutMucr.map(_.mucr)

    submissionService.submit(request.providerId, answers).map { conversationId =>
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
