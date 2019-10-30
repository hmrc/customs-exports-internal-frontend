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

package controllers.movements

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import forms.Location
import forms.Location.form
import javax.inject.{Inject, Singleton}
import models.cache._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.location

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  movementRepository: MovementRepository,
  mcc: MessagesControllerComponents,
  locationPage: location
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val location = request.answers match {
      case arrivalAnswers: ArrivalAnswers     => arrivalAnswers.location
      case departureAnswers: DepartureAnswers => departureAnswers.location
    }
    Ok(locationPage(location.fold(form)(form.fill(_))))
  }

  def saveLocation(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Location]) => Future.successful(BadRequest(locationPage(formWithErrors))),
        validForm => {
          request.answers match {
            case arrivalAnswers: ArrivalAnswers =>
              movementRepository.upsert(Cache(request.pid, arrivalAnswers.copy(location = Some(validForm)))).map { _ =>
                Redirect(controllers.movements.routes.SummaryController.displayPage())
              }
            case departureAnswers: DepartureAnswers =>
              movementRepository.upsert(Cache(request.pid, departureAnswers.copy(location = Some(validForm)))).map { _ =>
                Redirect(controllers.movements.routes.TransportController.displayPage())
              }
          }
        }
      )
  }
}