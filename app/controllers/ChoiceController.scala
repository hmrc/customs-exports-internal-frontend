/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.AuthenticatedAction
import controllers.exchanges.AuthenticatedRequest
import forms.Choice
import javax.inject.{Inject, Singleton}
import models.UcrType.{Ducr, DucrPart, Mucr}
import models.cache._
import models.{ReturnToStartException, UcrBlock}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject()(
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    cacheRepository.findByProviderId(request.providerId).map {
      case Some(cache) =>
        cache.answers
          .map(answers => Ok(choicePage(Choice.form().fill(Choice(answers.`type`)), cache.queryUcr)))
          .getOrElse(Ok(choicePage(Choice.form(), cache.queryUcr)))
      case None => Redirect(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
    }
  }

  def submit: Action[AnyContent] = authenticate.async { implicit request: AuthenticatedRequest[AnyContent] =>
    cacheRepository.findByProviderId(request.providerId).flatMap {
      case Some(cache) if cache.queryUcr.isDefined =>
        Choice.form().bindFromRequest().fold(formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))), proceed(_, cache))
      case _ =>
        Future.successful(Redirect(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm()))
    }
  }

  private def proceed(choice: Choice, cache: Cache)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] = choice match {
    case Choice.Arrival =>
      saveAndRedirect(ArrivalAnswers.fromQueryUcr, movements.routes.SpecificDateTimeController.displayPage())
    case Choice.RetrospectiveArrival =>
      saveAndRedirect(RetrospectiveArrivalAnswers.fromQueryUcr, movements.routes.LocationController.displayPage())
    case Choice.Departure =>
      saveAndRedirect(DepartureAnswers.fromQueryUcr, movements.routes.SpecificDateTimeController.displayPage())

    case Choice.AssociateUCR => {
      val redirectionCall = cache.queryUcr
        .map(_.ucrType match {
          case Ducr.codeValue | DucrPart.codeValue => consolidations.routes.MucrOptionsController.displayPage()
          case Mucr.codeValue                      => consolidations.routes.ManageMucrController.displayPage()
        })
        .getOrElse(throw ReturnToStartException)

      saveAndRedirect(AssociateUcrAnswers.fromQueryUcr, redirectionCall)
    }

    case Choice.DisassociateUCR =>
      saveAndRedirect(DisassociateUcrAnswers.fromQueryUcr, consolidations.routes.DisassociateUcrSummaryController.displayPage())
    case Choice.ShutMUCR =>
      saveAndRedirect(ShutMucrAnswers.fromQueryUcr, consolidations.routes.ShutMucrSummaryController.displayPage())
  }

  private def saveAndRedirect(answerProvider: Option[UcrBlock] => Answers, call: Call)(
    implicit request: AuthenticatedRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedCache: Cache <- cacheRepository.findByProviderId(request.providerId).map {
        case Some(cache) => cache.copy(answers = Some(answerProvider.apply(cache.queryUcr)))
        case None        => Cache(request.providerId, Some(answerProvider.apply(None)), None)
      }
      result <- cacheRepository.upsert(updatedCache).map(_ => Redirect(call))
    } yield (result)

}
