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
import forms.ChiefConsignment
import models.cache.Cache
import models.summary.SessionHelper
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUrlEncodedAndMultipartFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.manage_chief_consignment

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageChiefConsignmentController @Inject() (
  mcc: MessagesControllerComponents,
  authenticate: AuthenticatedAction,
  cacheRepository: CacheRepository,
  manageChiefConsignmentPage: manage_chief_consignment
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUrlEncodedAndMultipartFormBinding {

  val displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    val futureResult = cacheRepository
      .findByProviderId(request.providerId)
      .map {
        case Some(cache) =>
          cache.queryUcr.map(ucrBlock => getEmptyForm.fill(ChiefConsignment(ucrBlock))).getOrElse(getEmptyForm)

        case _ => getEmptyForm
      }
      .map(form => Ok(manageChiefConsignmentPage(form)))

    futureResult.map(_.withSession(SessionHelper.clearAllReceiptPageSessionKeys()))
  }

  val submitChiefConsignment: Action[AnyContent] = authenticate.async { implicit request =>
    getEmptyForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(manageChiefConsignmentPage(formWithErrors))),
        validChiefConsignment =>
          cacheRepository.upsert(Cache(request.providerId, validChiefConsignment.toUcrBlock)).map { _ =>
            Redirect(controllers.routes.ChoiceController.displayPage)
          }
      )
  }

  private def getEmptyForm: Form[ChiefConsignment] = ChiefConsignment.form()
}
