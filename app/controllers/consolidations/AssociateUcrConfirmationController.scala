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
import controllers.storage.FlashKeys
import javax.inject.Inject
import models.ReturnToStartException
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ucr_confirmation

import scala.concurrent.ExecutionContext

class AssociateUcrConfirmationController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  associateUCRConfirmPage: associate_ucr_confirmation
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def display: Action[AnyContent] = (authenticate andThen getJourney) { implicit request =>
    val summary = request.answers.summary.getOrElse(throw ReturnToStartException)
    Ok(associateUCRConfirmPage(summary.get(FlashKeys.CONSOLIDATION_KIND), summary.get(FlashKeys.UCR)))
  }

}
