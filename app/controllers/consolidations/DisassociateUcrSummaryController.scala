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
import controllers.storage.FlashKeys
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{DisassociateUcrAnswers, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.MovementRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.disassociate_ucr_summary

import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUcrSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  movementRepository: MovementRepository,
  page: disassociate_ucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def display: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)) { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(ucr))
      case _         => throw ReturnToStartException
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)).async { implicit request =>
    val answers = request.answersAs[DisassociateUcrAnswers]
    val ucr = answers.ucr.map(_.ucr).getOrElse(throw ReturnToStartException)
    val kind = answers.ucr.map(_.kind).getOrElse(throw ReturnToStartException)

    submissionService.submit(request.pid, answers).map { _ =>
      Redirect(controllers.consolidations.routes.DisassociateUcrConfirmationController.display())
        .flashing(FlashKeys.UCR -> ucr, FlashKeys.CONSOLIDATION_KIND -> kind.toString)
    }
  }

}
