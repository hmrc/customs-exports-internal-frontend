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
import controllers.exchanges.JourneyRequest
import controllers.summary.routes.ArriveDepartSummaryController
import forms.Transport
import forms.providers.TransportFormProvider

import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{DepartureAnswers, JourneyType}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.transport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  formProvider: TransportFormProvider,
  mcc: MessagesControllerComponents,
  transportPage: transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)) { implicit request =>
    val answers = request.answersAs[DepartureAnswers]
    val consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    answers.goodsDeparted match {
      case Some(_) => Ok(transportPage(answers.transport.fold(form)(form.fill(_)), consignmentReference))
      case None    => Redirect(routes.GoodsDepartedController.displayPage())
    }
  }

  def saveTransport(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)).async { implicit request =>
    val answers = request.answersAs[DepartureAnswers]
    def consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Transport]) => Future.successful(BadRequest(transportPage(formWithErrors, consignmentReference))),
        validForm => {
          val movementAnswers = answers.copy(transport = Some(validForm))
          cacheRepository.upsert(request.cache.update(movementAnswers)).map { _ =>
            Redirect(ArriveDepartSummaryController.displayPage())
          }
        }
      )
  }

  private def form(implicit request: JourneyRequest[_]): Form[Transport] = {
    val answers = request.answersAs[DepartureAnswers]
    formProvider.provideForm(answers)
  }

}
