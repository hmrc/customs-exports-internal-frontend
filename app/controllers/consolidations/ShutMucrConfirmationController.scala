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

import controllers.actions.AuthenticatedAction
import controllers.storage.FlashExtractor
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.JourneyType.SHUT_MUCR
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.confirmation_page

@Singleton
class ShutMucrConfirmationController @Inject() (
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  flashExtractor: FlashExtractor,
  confirmationPage: confirmation_page
) extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = authenticate { implicit request =>
    val journeyType = flashExtractor.extractMovementType(request).getOrElse(throw ReturnToStartException)
    val consignmentRefs = flashExtractor.extractConsignmentRefs(request)

    journeyType match {
      case SHUT_MUCR => Ok(confirmationPage(journeyType, consignmentRefs))
      case _         => throw ReturnToStartException
    }
  }

}
