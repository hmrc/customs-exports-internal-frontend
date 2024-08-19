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

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthenticatedAction
import javax.inject.Inject
import models.notifications.NotificationFrontendModel
import models.submissions.Submission
import models.viewmodels.notificationspage.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.view_notifications

import scala.concurrent.ExecutionContext

class ViewNotificationsController @Inject() (
  authenticate: AuthenticatedAction,
  connector: CustomsDeclareExportsMovementsConnector,
  factory: NotificationPageSingleElementFactory,
  mcc: MessagesControllerComponents,
  viewNotificationPage: view_notifications
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def listOfNotifications(conversationId: String): Action[AnyContent] = authenticate.async { implicit request =>
    val providerId = request.providerId

    val params = for {
      submission: Option[Submission] <- connector.fetchSingleSubmission(conversationId, providerId)
      submissionElement: Option[NotificationsPageSingleElement] = submission.map(factory.build)

      submissionNotifications: Seq[NotificationFrontendModel] <- connector.fetchNotifications(conversationId, providerId)
      notificationElements: Seq[NotificationsPageSingleElement] = submissionNotifications.sorted.map(factory.build)

      submissionUcr = submission.flatMap(_.extractUcr)
    } yield (submissionUcr, submissionElement, notificationElements)

    params.map {
      case (Some(submissionUcr), Some(submissionElement), notificationElements) =>
        val elementsToDisplay = submissionElement +: notificationElements
        Ok(viewNotificationPage(submissionUcr, elementsToDisplay.reverse))

      case _ =>
        Redirect(routes.ViewSubmissionsController.displayPage)
    }
  }
}
