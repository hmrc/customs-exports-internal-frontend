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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Results}
import controllers.actions.{ArrivalAction, AuthenticatedAction}
import controllers.exchanges.{ArrivalRequest, AuthenticatedRequest}
import models.cache.{Arrival, Cache}
import repositories.MovementRepository
import views.html.choice_page
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject()(
                                  authenticate: AuthenticatedAction,
                                  verifyArrival: ArrivalAction,
                                  mcc: MessagesControllerComponents,
                                  movementRepository: MovementRepository,
                                  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    movementRepository.findByPid("PID").map {
      case Some(cache) => Ok(choicePage(Choice.form.fill(Choice(cache.answers.`type`))))
      case None => Ok(choicePage(Choice.form))
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request: AuthenticatedRequest[AnyContent] =>
    Choice.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))),
        saveChoice
      )
  }

  def secondPage(): Action[AnyContent] = (authenticate andThen verifyArrival).async { implicit request: ArrivalRequest[AnyContent] =>
    val arrival: Arrival = request.answers
    Future.successful(Results.Ok(""))
  }

  private def saveChoice(choice: Choice): Future[Result] = {
    if(choice.value == "arrival") {
      val answers = Arrival(None)
      movementRepository.findOrCreate("PID", Cache("PID", answers)).flatMap { cache =>
        val newCache = cache.copy(answers = answers)
        movementRepository.insert(newCache).map { _ =>
          Redirect(routes.ChoiceController.displayChoiceForm())
        }
      }
    } else {
      Future.successful(Redirect(routes.ChoiceController.displayChoiceForm()))
    }
  }
}
