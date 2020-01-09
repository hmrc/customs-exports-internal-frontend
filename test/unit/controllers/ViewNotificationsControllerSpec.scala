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

package controllers

import connectors.CustomsDeclareExportsMovementsConnector
import models.notifications.{NotificationFrontendModel, ResponseType}
import models.submissions.{ActionType, Submission}
import models.viewmodels.notificationspage.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.{conversationId, providerId, validEori}
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.view_notifications

import scala.concurrent.{ExecutionContext, Future}

class ViewNotificationsControllerSpec extends ControllerLayerSpec with ScalaFutures {

  implicit val messages: Messages = stubMessages()
  private val customsExportsMovementsConnectorMock: CustomsDeclareExportsMovementsConnector = mock[CustomsDeclareExportsMovementsConnector]
  private val notificationPageSingleElementFactoryMock: NotificationPageSingleElementFactory = mock[NotificationPageSingleElementFactory]
  private val notificationsPageMock: view_notifications = mock[view_notifications]

  private val expectedSubmission = exampleSubmission()
  private val expectedNotifications =
    Seq(exampleNotificationFrontendModel(), exampleNotificationFrontendModel(responseType = ResponseType.MovementTotalsResponse))
  private val singleElementForSubmission = NotificationsPageSingleElement("REQUEST", "", HtmlFormat.empty)
  private val singleElementForNotification = NotificationsPageSingleElement("RESPONSE", "", HtmlFormat.empty)

  private val controller = new ViewNotificationsController(
    SuccessfulAuth(),
    customsExportsMovementsConnectorMock,
    notificationPageSingleElementFactoryMock,
    stubMessagesControllerComponents(),
    notificationsPageMock
  )(ExecutionContext.global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(customsExportsMovementsConnectorMock.fetchSingleSubmission(any(), any())(any())).thenReturn(Future.successful(Some(expectedSubmission)))
    when(customsExportsMovementsConnectorMock.fetchNotifications(any(), any())(any())).thenReturn(Future.successful(expectedNotifications))
    when(notificationPageSingleElementFactoryMock.build(any[Submission])(any())).thenReturn(singleElementForSubmission)
    when(notificationPageSingleElementFactoryMock.build(any[NotificationFrontendModel])(any())).thenReturn(singleElementForNotification)
    when(notificationsPageMock.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(customsExportsMovementsConnectorMock, notificationsPageMock, notificationPageSingleElementFactoryMock)

    super.afterEach()
  }

  "NotificationController on listOfNotifications" when {

    "everything works correctly" should {

      "call CustomsExportsMovementsConnector.fetchSingleSubmission, passing conversation ID and EORI provided" in {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(customsExportsMovementsConnectorMock).fetchSingleSubmission(meq(conversationId), meq(providerId))(any())
      }

      "call CustomsExportsMovementsConnector.fetchNotifications, passing conversation ID and EORI provided" in {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(customsExportsMovementsConnectorMock).fetchNotifications(meq(conversationId), meq(providerId))(any())
      }

      "call NotificationPageSingleElementFactory, passing models returned by Connector" in {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(notificationPageSingleElementFactoryMock).build(meq(expectedSubmission))(any())
        expectedNotifications.foreach(expNotification => verify(notificationPageSingleElementFactoryMock).build(meq(expNotification))(any()))
      }

      "call notification view template, passing UCR related to the submission" in {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        val expectedUcr: String = expectedSubmission.ucrBlocks.head.ucr

        verify(notificationsPageMock).apply(meq(expectedUcr), any[NotificationsPageSingleElement], any[Seq[NotificationsPageSingleElement]])(
          any(),
          any()
        )
      }

      "call notification view template, passing data returned by NotificationsPageSingleElementFactory" in {

        val expectedViewInput: Seq[NotificationsPageSingleElement] =
          expectedNotifications.map(_ => singleElementForNotification)

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(notificationsPageMock).apply(any[String], meq(singleElementForSubmission), meq(expectedViewInput))(any(), any())
      }

      "return 200 (OK)" in {

        val result = controller.listOfNotifications(conversationId)(FakeRequest())

        status(result) must be(OK)
      }
    }

    "submission is missing UCR" should {

      "redirect back to movements" in {

        when(customsExportsMovementsConnectorMock.fetchSingleSubmission(any(), any())(any()))
          .thenReturn(
            Future
              .successful(Some(Submission(eori = validEori, conversationId = conversationId, ucrBlocks = Seq.empty, actionType = ActionType.Arrival)))
          )

        val result = controller.listOfNotifications(conversationId)(FakeRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ViewSubmissionsController.displayPage().url)
      }
    }
  }

}
