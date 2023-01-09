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

package controllers.summary

import controllers.actions.AuthenticatedAction
import models.ReturnToStartException
import models.summary.Confirmation
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.confirmation_page

import javax.inject.{Inject, Singleton}

@Singleton
class MovementConfirmationController @Inject() (
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  confirmationPage: confirmation_page
) extends FrontendController(mcc) with I18nSupport {

  val displayPage: Action[AnyContent] = authenticate { implicit request =>
    Confirmation()
      .map(confirmation => Ok(confirmationPage(confirmation)))
      .getOrElse(throw ReturnToStartException)
  }
}
