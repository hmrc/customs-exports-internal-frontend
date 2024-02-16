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

package controllers

import controllers.actions.AuthenticatedAction
import forms.ChiefUcrDetails
import models.cache.Cache
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ducr_part_details

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DucrPartDetailsController @Inject() (
  mcc: MessagesControllerComponents,
  authenticate: AuthenticatedAction,
  cacheRepository: CacheRepository,
  ducrPartsDetailsPage: ducr_part_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    cacheRepository
      .findByProviderId(request.providerId)
      .map {
        case Some(cache) =>
          cache.queryUcr.map(ucrBlock => getEmptyForm.fill(ChiefUcrDetails(ucrBlock))).getOrElse(getEmptyForm)

        case _ => getEmptyForm
      }
      .map(form => Ok(ducrPartsDetailsPage(form)))
  }

  val submitDucrPartDetails: Action[AnyContent] = authenticate.async { implicit request =>
    getEmptyForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
        validDucrPartDetails =>
          cacheRepository.upsert(Cache(request.providerId, validDucrPartDetails.toUcrBlock)).map { _ =>
            Redirect(controllers.routes.ChoiceController.displayPage)
          }
      )
  }

  private def getEmptyForm: Form[ChiefUcrDetails] = ChiefUcrDetails.form()
}
