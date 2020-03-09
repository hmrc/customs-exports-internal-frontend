/*
 * Copyright 2020 HM Revenue & Customs
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
import models.cache.{AssociateUcrAnswers, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associateucr.associate_ucr_summary

import scala.concurrent.ExecutionContext

class AssociateUCRSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    if (isCacheDataValid(answers))
      Ok(associateUcrSummaryPage(answers))
    else
      throw ReturnToStartException
  }

  private def isCacheDataValid(answers: AssociateUcrAnswers): Boolean = answers.parentMucr.isDefined && answers.childUcr.isDefined

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)).async { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]

    submissionService.submit(request.providerId, answers).map { _ =>
      Redirect(controllers.consolidations.routes.AssociateUCRConfirmationController.displayPage())
        .flashing(FlashKeys.MOVEMENT_TYPE -> request.answers.`type`.toString)
    }
  }
}
