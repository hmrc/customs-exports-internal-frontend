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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.consolidations.{routes => consolidationRoutes}
import forms.AssociateUcr.form
import javax.inject.Inject
import models.cache.{AssociateUcrAnswers, Cache}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.MovementRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ucr

import scala.concurrent.{ExecutionContext, Future}

class AssociateUCRController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  movementRepository: MovementRepository,
  associateUcrPage: associate_ucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val associateUcrAnswers = request.answersAs[AssociateUcrAnswers]
    val mucrOptions = associateUcrAnswers.mucrOptions
    val associateUcr = associateUcrAnswers.associateUcr

    mucrOptions match {
      case Some(mucrOptions) => Ok(associateUcrPage(associateUcr.fold(form)(form.fill), mucrOptions))
      case None              => Redirect(controllers.routes.ChoiceController.displayPage())
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          request.answersAs[AssociateUcrAnswers].mucrOptions match {
            case Some(mucr) => Future.successful(BadRequest(associateUcrPage(formWithErrors, mucr)))
            case None       => Future.successful(Redirect(controllers.routes.ChoiceController.displayPage()))
          }
        },
        formData => {
          val updatedCache = request.answersAs[AssociateUcrAnswers].copy(associateUcr = Some(formData))
          movementRepository.upsert(Cache(request.pid, updatedCache)).map { _ =>
            Redirect(consolidationRoutes.AssociateUCRSummaryController.displayPage())
          }
        }
      )
  }
}
