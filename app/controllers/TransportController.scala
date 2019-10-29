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
import forms.Transport
import forms.Transport.form
import javax.inject.{Inject, Singleton}
import models.cache.{Cache, MovementAnswers}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.transport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  movementRepository: MovementRepository,
  mcc: MessagesControllerComponents,
  transportPage: transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    Future.successful(Ok(transportPage(request.answersAs[MovementAnswers].transport.fold(form)(form.fill(_)))))
  }

  def saveTransport(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Transport]) => Future.successful(BadRequest(transportPage(formWithErrors))),
        validForm => {
          val movementAnswers = request.answersAs[MovementAnswers].copy(transport = Some(validForm))
          movementRepository.upsert(Cache(request.pid, movementAnswers)).map { _ =>
            Redirect(controllers.routes.SummaryController.displayPage())
          }
        }
      )
  }
}
