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
import javax.inject.Inject
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{associate_ucr_confirmation, associate_ucr_summary}
import play.api.mvc.Results.Redirect
import controllers.storage.FlashKeys

import scala.concurrent.ExecutionContext

class AssociateUCRSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val mucrOpt = answers.mucrOptions
    val associateUcrOpt = answers.associateUcr

    (mucrOpt, associateUcrOpt) match {
      case (Some(mucrOptions), Some(ucr)) => Ok(associateUcrSummaryPage(ucr, mucrOptions.mucr))
      case _                              => throw ReturnToStartException
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val associateUcr = answers.associateUcr.getOrElse(throw ReturnToStartException)

    submissionService.submit(request.providerId, answers).map { _ =>
      Redirect(controllers.consolidations.routes.AssociateUCRConfirmationController.display())
        .flashing(FlashKeys.CONSOLIDATION_KIND -> associateUcr.kind.formValue, FlashKeys.UCR -> associateUcr.ucr)
    }
  }
}
