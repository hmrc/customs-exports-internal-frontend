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

package controllers.movements

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.exchanges.JourneyRequest
import forms.{ArrivalDetails, DepartureDetails, MovementDetails}
import models.ReturnToStartException
import models.cache._
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{arrival_details, departure_details}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovementDetailsController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  details: MovementDetails,
  arrivalDetailsPage: arrival_details,
  departureDetailsPage: departure_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)) { implicit request =>
    request.answers match {
      case arrivalAnswers: ArrivalAnswers     => Ok(arrivalPage(arrivalAnswers))
      case departureAnswers: DepartureAnswers => Ok(departurePage(departureAnswers))
      case _                                  => BadRequest
    }
  }

  val saveMovementDetails: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)).async { implicit request =>
    (request.answers match {
      case arrivalAnswers: ArrivalAnswers     => handleSavingArrival(arrivalAnswers)
      case departureAnswers: DepartureAnswers => handleSavingDeparture(departureAnswers)
      case _                                  => throw new IllegalArgumentException("Invalid answers type")
    }).flatMap {
      case Left(resultView) => Future.successful(BadRequest(resultView))
      case Right(call)      => Future.successful(Redirect(call))
    }
  }

  private def arrivalPage(arrivalAnswers: ArrivalAnswers)(implicit request: JourneyRequest[AnyContent]): Html =
    arrivalDetailsPage(arrivalAnswers.arrivalDetails.fold(details.arrivalForm)(details.arrivalForm.fill(_)), arrivalAnswers.consignmentReferences)

  private def departurePage(departureAnswers: DepartureAnswers)(implicit request: JourneyRequest[AnyContent]): Html =
    departureDetailsPage(
      departureAnswers.departureDetails.fold(details.departureForm)(details.departureForm.fill(_)),
      departureAnswers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    )

  private def handleSavingArrival(arrivalAnswers: ArrivalAnswers)(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] = {
    def withDateSpecificErrors(formWithErrors: Form[ArrivalDetails]) = formWithErrors.copy(errors = formLevelErrors("Arrival", formWithErrors.errors))
    details.arrivalForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ArrivalDetails]) =>
          Future.successful(Left(arrivalDetailsPage(withDateSpecificErrors(formWithErrors), arrivalAnswers.consignmentReferences))),
        validForm =>
          cacheRepository.upsert(request.cache.update(arrivalAnswers.copy(arrivalDetails = Some(validForm)))).map { _ =>
            Right(controllers.movements.routes.LocationController.displayPage)
          }
      )
  }

  private def handleSavingDeparture(departureAnswers: DepartureAnswers)(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] = {
    def consignmentReference = departureAnswers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    def withDateSpecificErrors(formWithErrors: Form[DepartureDetails]) =
      formWithErrors.copy(errors = formLevelErrors("Departure", formWithErrors.errors))
    details.departureForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DepartureDetails]) =>
          Future.successful(Left(departureDetailsPage(withDateSpecificErrors(formWithErrors), consignmentReference))),
        validForm =>
          cacheRepository.upsert(request.cache.update(departureAnswers.copy(departureDetails = Some(validForm)))).map { _ =>
            Right(controllers.movements.routes.LocationController.displayPage)
          }
      )
  }

  private def formLevelErrors(keySuffix: String, errors: Seq[FormError]) =
    errors.headOption
      .filter(_.key.isEmpty)
      .map(err => Seq(FormError(s"dateOf$keySuffix", err.message), FormError(s"timeOf$keySuffix", err.message)))
      .getOrElse(errors)
}
