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

package controllers.movements

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.summary.routes._
import controllers.exchanges.JourneyRequest
import forms.Location
import forms.Location.form

import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.location

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  locationPage: location
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.RETROSPECTIVE_ARRIVE, JourneyType.DEPART)) { implicit request =>
      val location = request.answersAs[MovementAnswers].location

      Ok(buildPage(location.fold(form())(form().fill(_))))
    }

  def saveLocation(): Action[AnyContent] =
    (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.RETROSPECTIVE_ARRIVE, JourneyType.DEPART)).async { implicit request =>
      if (request.answersAs[MovementAnswers].consignmentReferences.map(_.referenceValue).isEmpty) throw ReturnToStartException

      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Location]) => Future.successful(BadRequest(buildPage(formWithErrors))),
          validLocation =>
            request.answers match {
              case arrivalAnswers: ArrivalAnswers =>
                cacheRepository.upsert(request.cache.update(arrivalAnswers.copy(location = Some(validLocation)))).map { _ =>
                  Redirect(ArriveDepartSummaryController.displayPage())
                }
              case retroArrivalAnswers: RetrospectiveArrivalAnswers =>
                cacheRepository.upsert(request.cache.update(retroArrivalAnswers.copy(location = Some(validLocation)))).map { _ =>
                  Redirect(ArriveDepartSummaryController.displayPage())
                }
              case departureAnswers: DepartureAnswers =>
                cacheRepository.upsert(request.cache.update(departureAnswers.copy(location = Some(validLocation)))).map { _ =>
                  Redirect(controllers.movements.routes.GoodsDepartedController.displayPage())
                }
              case _ => Future.successful(BadRequest)
            }
        )
    }

  private def buildPage(form: Form[Location])(implicit request: JourneyRequest[_]) = {
    val answers = request.answersAs[MovementAnswers]
    locationPage(form, answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException), answers.specificDateTimeChoice)
  }
}
