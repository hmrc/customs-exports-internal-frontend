/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.exchanges.JourneyRequest
import forms.ManageMucrChoice._
import forms.{AssociateUcr, ManageMucrChoice, MucrOptions}

import javax.inject.{Inject, Singleton}
import models.UcrType.Mucr
import models.cache.AssociateUcrAnswers
import models.cache.JourneyType.ASSOCIATE_UCR
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.associateucr.manage_mucr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageMucrController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  manageMucrPage: manage_mucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(ASSOCIATE_UCR)) { implicit request =>
    request.cache.queryUcr.map(_.ucrType) match {
      case Some(Mucr.codeValue) =>
        val manageMucrChoice = request.answersAs[AssociateUcrAnswers].manageMucrChoice
        Ok(manageMucrPage(manageMucrChoice.fold(form())(form().fill), request.cache.queryUcr))
      case _ => throw new IllegalStateException("")
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(ASSOCIATE_UCR)).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(manageMucrPage(formWithErrors, request.cache.queryUcr))),
        validManageMucrChoice => {
          val updatedCache = request.cache.update(updateAnswersWith(validManageMucrChoice))

          cacheRepository.upsert(updatedCache).map { _ =>
            validManageMucrChoice.choice match {
              case AssociateThisToMucr       => Redirect(routes.MucrOptionsController.displayPage())
              case AssociateAnotherUcrToThis => Redirect(routes.AssociateUcrController.displayPage())
            }
          }
        }
      )
  }

  private def updateAnswersWith(manageMucrChoice: ManageMucrChoice)(implicit request: JourneyRequest[AnyContent]): AssociateUcrAnswers = {
    val oldAnswers = request.answersAs[AssociateUcrAnswers]
    val answersWithManageMucrChoiceUpdated = oldAnswers.copy(manageMucrChoice = Some(manageMucrChoice))

    manageMucrChoice.choice match {
      case AssociateThisToMucr =>
        answersWithManageMucrChoiceUpdated.copy(parentMucr = None, childUcr = request.cache.queryUcr.map(AssociateUcr(_)))
      case AssociateAnotherUcrToThis =>
        answersWithManageMucrChoiceUpdated.copy(parentMucr = request.cache.queryUcr.map(MucrOptions(_)), childUcr = None)
    }
  }

}
