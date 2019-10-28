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
import javax.inject.Inject
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ucr_summary

import scala.concurrent.ExecutionContext

class AssociateUCRSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  movementRepository: MovementRepository,
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

  def submit(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    //MUCR options necessary for submit
    val mucrOptions = answers.mucrOptions.getOrElse(throw ReturnToStartException)
    val associateUcr = answers.associateUcr.getOrElse(throw ReturnToStartException)

    Redirect(routes.AssociateUCRConfirmationController.displayPage())
      .flashing(FlashKeys.UCR -> associateUcr.ucr, FlashKeys.CONSOLIDATION_KIND -> associateUcr.kind.formValue)
  }
}
