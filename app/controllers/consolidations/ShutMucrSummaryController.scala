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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{shut_mucr_confirmation, shut_mucr_summary}

import scala.concurrent.ExecutionContext

@Singleton
class ShutMucrSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  summaryPage: shut_mucr_summary,
  confirmationPage: shut_mucr_confirmation
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val mucr = request.answersAs[ShutMucrAnswers].shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)
    Ok(summaryPage(mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    val answers = request.answersAs[ShutMucrAnswers]
    submissionService.submit(request.pid, answers).map { _ =>
      Ok(confirmationPage(answers.shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)))
    }
  }
}
