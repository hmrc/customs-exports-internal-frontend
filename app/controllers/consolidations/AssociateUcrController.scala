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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.summary.routes.AssociateUcrSummaryController
import forms.AssociateUcr.form
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUrlEncodedAndMultipartFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.associateucr.associate_ucr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssociateUcrController @Inject() (
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  associateUcrPage: associate_ucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUrlEncodedAndMultipartFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val associateUcrAnswers = request.answersAs[AssociateUcrAnswers]
    val mucrOptions = associateUcrAnswers.parentMucr.getOrElse(throw ReturnToStartException)
    val associateUcr = associateUcrAnswers.childUcr

    Ok(associateUcrPage(associateUcr.fold(form)(form.fill), mucrOptions))
  }

  val submit: Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    val mucrOptions = request.answersAs[AssociateUcrAnswers].parentMucr.getOrElse(throw ReturnToStartException)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(BadRequest(associateUcrPage(formWithErrors, mucrOptions)))
        },
        formData => {
          val updatedCache = request.answersAs[AssociateUcrAnswers].copy(childUcr = Some(formData))
          cacheRepository.upsert(request.cache.update(updatedCache)).map { _ =>
            Redirect(AssociateUcrSummaryController.displayPage)
          }
        }
      )
  }
}
