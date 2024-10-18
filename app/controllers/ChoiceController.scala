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

package controllers

import controllers.actions.AuthenticatedAction
import controllers.exchanges.AuthenticatedRequest
import controllers.ileQuery.routes.FindConsignmentController
import controllers.summary.routes._
import forms.Choice
import models.{ReturnToStartException, UcrBlock}
import models.UcrType.{Ducr, DucrPart, Mucr}
import models.cache._
import models.summary.SessionHelper
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject() (
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    val futureResult = cacheRepository.findByProviderId(request.providerId).map {
      case Some(cache) =>
        cache.answers
          .map(answers => Ok(choicePage(Choice.form().fill(Choice(answers.`type`)), cache.queryUcr)))
          .getOrElse(Ok(choicePage(Choice.form(), cache.queryUcr)))

      case None => Redirect(FindConsignmentController.displayQueryForm)
    }

    futureResult.map(_.withSession(SessionHelper.clearAllReceiptPageSessionKeys()))
  }

  val submit: Action[AnyContent] = authenticate.async { implicit request: AuthenticatedRequest[AnyContent] =>
    cacheRepository.findByProviderId(request.providerId).flatMap {
      case Some(cache) if cache.queryUcr.isDefined =>
        Choice
          .form()
          .bindFromRequest()
          .fold(formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))), proceed(_, cache))

      case _ =>
        Future.successful(Redirect(FindConsignmentController.displayQueryForm))
    }
  }

  private def proceed(choice: Choice, cache: Cache): Future[Result] = {
    def determineRedirectionForAssociateUcr(queryUcr: Option[UcrBlock]): Call = queryUcr.map { ucrBlock =>
      ucrBlock.ucrType match {
        case Ducr.codeValue | DucrPart.codeValue => consolidations.routes.MucrOptionsController.displayPage
        case Mucr.codeValue                      => consolidations.routes.ManageMucrController.displayPage
        case _                                   => throw ReturnToStartException
      }
    }.getOrElse(throw ReturnToStartException)

    val (answers, call) = choice match {
      case Choice.Arrival =>
        (ArrivalAnswers.fromQueryUcr(cache.queryUcr), movements.routes.SpecificDateTimeController.displayPage)

      case Choice.RetrospectiveArrival if cache.queryUcr.exists(_.isChief) =>
        (ArrivalAnswers.fromQueryUcr(cache.queryUcr).withLocationCode("GBAURETRETRET"), movements.routes.SpecificDateTimeController.displayPage)

      case Choice.RetrospectiveArrival =>
        (RetrospectiveArrivalAnswers.fromQueryUcr(cache.queryUcr), movements.routes.LocationController.displayPage)

      case Choice.Departure =>
        (DepartureAnswers.fromQueryUcr(cache.queryUcr), movements.routes.SpecificDateTimeController.displayPage)

      case Choice.AssociateUCR =>
        val redirection = determineRedirectionForAssociateUcr(cache.queryUcr)
        (AssociateUcrAnswers.fromQueryUcr(cache.queryUcr), redirection)

      case Choice.DisassociateUCR =>
        (DisassociateUcrAnswers.fromQueryUcr(cache.queryUcr), DisassociateUcrSummaryController.displayPage)

      case Choice.ShutMUCR =>
        (ShutMucrAnswers.fromQueryUcr(cache.queryUcr), ShutMucrSummaryController.displayPage)
    }

    saveAndRedirect(cache.copy(answers = Some(answers)), call)
  }

  private def saveAndRedirect(updatedCache: Cache, call: Call): Future[Result] =
    cacheRepository.upsert(updatedCache).map(_ => Redirect(call))
}
