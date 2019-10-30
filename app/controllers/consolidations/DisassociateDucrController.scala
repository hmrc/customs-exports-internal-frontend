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
import forms.DisassociateDucr
import javax.inject.{Inject, Singleton}
import models.cache.{DisassociateUcrAnswers, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.MovementRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.disassociate_ducr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisassociateDucrController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  movementRepository: MovementRepository,
  page: disassociate_ducr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def display: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)) { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(DisassociateDucr.form.fill(ucr)))
      case _         => Ok(page(DisassociateDucr.form))
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)).async { implicit request =>
    DisassociateDucr.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(page(formWithErrors))),
        answers =>
          submissionService.submit(request.pid, request.answersAs[DisassociateUcrAnswers].copy(ucr = Some(answers))).map { _ =>
            Redirect(controllers.consolidations.routes.DisassociateDucrConfirmationController.display())
              .flashing(FlashKeys.UCR -> answers)
        }
      )
  }

}
