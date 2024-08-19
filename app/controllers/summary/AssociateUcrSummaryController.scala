/*
 * Copyright 2024 HM Revenue & Customs
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
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, JourneyType}
import models.summary.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.associate_ucr_summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AssociateUcrSummaryController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    if (isCacheDataValid(answers))
      Ok(associateUcrSummaryPage(answers))
    else
      throw ReturnToStartException
  }

  private def isCacheDataValid(answers: AssociateUcrAnswers): Boolean = answers.parentMucr.isDefined && answers.childUcr.isDefined

  val submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)).async { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val ucrType = answers.childUcr.map(_.kind.codeValue)
    val ucr = answers.childUcr.map(_.ucr)
    val mucrToAssociate = answers.parentMucr.map(_.mucr)

    submissionService.submit(request.providerId, answers).map { conversationId =>
      val values = List(
        Some(JOURNEY_TYPE -> answers.`type`.toString),
        ucr.map(ucr => UCR -> ucr),
        ucrType.map(ucrType => UCR_TYPE -> ucrType),
        mucrToAssociate.map(mucr => MUCR_TO_ASSOCIATE -> mucr),
        Some(CONVERSATION_ID -> conversationId)
      ).flatten

      Redirect(MovementConfirmationController.displayPage).addingToSession(values: _*)
    }
  }
}
