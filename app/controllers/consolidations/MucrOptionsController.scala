/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.consolidations.{routes => consolidationsRoutes}
import forms.MucrOptions.form
import javax.inject.Inject
import models.cache.{AssociateUcrAnswers, Cache}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.mucr_options

import scala.concurrent.{ExecutionContext, Future}

class MucrOptionsController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  mucrOptionsPage: mucr_options
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val mucrOptions = request.answersAs[AssociateUcrAnswers].mucrOptions

    Ok(mucrOptionsPage(mucrOptions.fold(form)(form.fill)))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(mucrOptionsPage(formWithErrors))),
        validForm => {
          val updatedCache = request.answersAs[AssociateUcrAnswers].copy(mucrOptions = Some(validForm))
          cacheRepository.upsert(Cache(request.providerId, updatedCache)).map { _ =>
            Redirect(consolidationsRoutes.AssociateUCRController.displayPage())
          }
        }
      )
  }
}
