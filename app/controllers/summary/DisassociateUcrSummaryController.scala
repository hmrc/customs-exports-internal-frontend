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
import models.ReturnToStartException
import models.cache.{DisassociateUcrAnswers, JourneyType}
import models.summary.FlashKeys
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.disassociate_ucr_summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUcrSummaryController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  page: disassociate_ucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)) { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(ucr))
      case _         => throw ReturnToStartException
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)).async { implicit request =>
    val answers = request.answersAs[DisassociateUcrAnswers]
    val ucrType = answers.ucr.map(_.kind.codeValue)
    val ucr = answers.ucr.map(_.ucr)

    submissionService.submit(request.providerId, answers).map { conversationId =>
      val flash = Seq(
        Some(FlashKeys.JOURNEY_TYPE -> answers.`type`.toString),
        ucr.map(ucr => FlashKeys.UCR -> ucr),
        ucrType.map(ucrType => FlashKeys.UCR_TYPE -> ucrType),
        Some(FlashKeys.CONVERSATION_ID -> conversationId)
      ).flatten

      Redirect(controllers.summary.routes.MovementConfirmationController.displayPage())
        .flashing(flash: _*)
    }
  }

}
