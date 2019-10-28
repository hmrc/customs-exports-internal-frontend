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

package controllers

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import forms.ArrivalReference
import forms.ArrivalReference.form
import javax.inject.{Inject, Singleton}
import models.cache.{ArrivalAnswers, Cache}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.arrival_reference

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArrivalReferenceController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  movementRepository: MovementRepository,
  mcc: MessagesControllerComponents,
  arrivalReferencePage: arrival_reference
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    Future.successful(Ok(arrivalReferencePage(request.answersAs[ArrivalAnswers].arrivalReference.fold(form)(form.fill(_)))))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ArrivalReference]) => Future.successful(BadRequest(arrivalReferencePage(formWithErrors))),
        validForm => {
          val arrivalAnswers = request.answersAs[ArrivalAnswers].copy(arrivalReference = Some(validForm))
          movementRepository.upsert(Cache(request.operator.pid, arrivalAnswers)).map { _ =>
            // TODO movement details controller
            Redirect(controllers.routes.MovementDetailsController.displayPage())

          }
        }
      )
  }
}
