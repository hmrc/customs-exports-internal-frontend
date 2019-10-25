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

import forms.Choice
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result, Results}
import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.exchanges.{AuthenticatedRequest, JourneyRequest}
import models.cache.{
  Answers,
  ArrivalAnswers,
  AssociateUcrAnswers,
  Cache,
  DepartureAnswers,
  DissociateUcrAnswers,
  ShutMucrAnswers
}
import repositories.MovementRepository
import views.html.choice_page
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  movementRepository: MovementRepository,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
  println("#### " + request.operator.pid)
    movementRepository.findByPid(request.operator.pid).map {
      case Some(cache) =>
        println("SOME")
        Ok(choicePage(Choice.form().fill(Choice(cache.answers.`type`))))
      case None        =>
        println("NONE")
        Ok(choicePage(Choice.form()))
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request: AuthenticatedRequest[AnyContent] =>
    Choice
      .form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))),
        _ match {
          case forms.Choice.Arrival =>
            proceedJourney(ArrivalAnswers(None), routes.ChoiceController.displayChoiceForm())
          case forms.Choice.Departure =>
            proceedJourney(DepartureAnswers(None), routes.ChoiceController.displayChoiceForm())
          case forms.Choice.AssociateUCR =>
            proceedJourney(AssociateUcrAnswers(None), routes.ChoiceController.displayChoiceForm())
          case forms.Choice.DisassociateUCR =>
            proceedJourney(DissociateUcrAnswers(None), routes.ChoiceController.displayChoiceForm())
          case forms.Choice.ShutMUCR =>
            proceedJourney(ShutMucrAnswers(None), routes.ChoiceController.displayChoiceForm())
        }
      )
  }

  private def proceedJourney(journey: Answers, call: Call)(
    implicit request: AuthenticatedRequest[AnyContent]
  ): Future[Result] =
    // TODO change to upsert
    movementRepository.findOrCreate(request.pid, Cache(request.pid, journey)).map(_ => Redirect(call))
}
