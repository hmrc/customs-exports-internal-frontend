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

package controllers.movements

import controllers.actions.AuthenticatedAction
import controllers.storage.FlashKeys
import forms.ConsignmentReferences
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.JourneyType
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.movement_confirmation_page

import scala.concurrent.ExecutionContext

@Singleton
class MovementConfirmationController @Inject()(authenticate: AuthenticatedAction, mcc: MessagesControllerComponents, page: movement_confirmation_page)(
  implicit ec: ExecutionContext
) extends FrontendController(mcc) with I18nSupport {

  def display: Action[AnyContent] = authenticate { implicit request =>
    val flash = implicitly[Flash]
    val `type` = flash.get(FlashKeys.MOVEMENT_TYPE).map(JourneyType.withName).getOrElse(throw ReturnToStartException)
    val kind = flash.get(FlashKeys.UCR_KIND).getOrElse(throw ReturnToStartException)
    val reference = flash.get(FlashKeys.UCR).getOrElse(throw ReturnToStartException)
    Ok(page(`type`, ConsignmentReferences(kind, reference)))
  }

}
