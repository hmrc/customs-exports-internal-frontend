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

package controllers

import java.time.Instant

import connectors.CustomsDeclareExportsMovementsConnector
import models.notifications.NotificationFrontendModel
import models.submissions.Submission
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.{conversationId, conversationId_2, conversationId_3, providerId}
import testdata.MovementsTestData
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.view_submissions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewSubmissionsControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val submissionsPage = mock[view_submissions]
  private val customsExportsMovementConnector: CustomsDeclareExportsMovementsConnector = mock[CustomsDeclareExportsMovementsConnector]
  private val controller =
    new ViewSubmissionsController(SuccessfulAuth(), customsExportsMovementConnector, stubMessagesControllerComponents(), submissionsPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(customsExportsMovementConnector)
    when(submissionsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionsPage)

    super.afterEach()
  }

  "SubmissionController on displayPage" should {

    "return 200 (OK)" in {

      when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))
      when(customsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      val result = controller.displayPage(getRequest)

      status(result) mustBe OK
    }

    "call connector for all Submissions" in {

      when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))
      when(customsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      controller.displayPage(getRequest).futureValue

      val expectedProviderId = providerId
      verify(customsExportsMovementConnector).fetchAllSubmissions(meq(expectedProviderId))(any())
    }

    "call connector for all Notifications" in {

      val submission = MovementsTestData.exampleSubmission()
      when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq(submission)))
      when(customsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      controller.displayPage(getRequest).futureValue

      val expectedProviderId = providerId
      verify(customsExportsMovementConnector).fetchAllNotificationsForUser(meq(expectedProviderId))(any())
    }

    "call submissions view, passing Submissions in descending order" when {

      "there are no Notifications for the Submissions" in {

        val submission1 = exampleSubmission(requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmission(requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmission(requestTimestamp = Instant.now())

        when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(customsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

        controller.displayPage(getRequest).futureValue

        val viewArguments: Seq[(Submission, Seq[NotificationFrontendModel])] = captureViewArguments()

        val submissions: Seq[Submission] = viewArguments.map(_._1)
        submissions mustBe Seq(submission3, submission2, submission1)
      }

      "there are Notifications for the Submissions" in {

        val submission1 = exampleSubmission(conversationId = conversationId, requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmission(conversationId = conversationId_2, requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmission(conversationId = conversationId_3, requestTimestamp = Instant.now())

        val notification1 = exampleNotificationFrontendModel(conversationId = conversationId)
        val notification2 = exampleNotificationFrontendModel(conversationId = conversationId_2)
        val notification3 = exampleNotificationFrontendModel(conversationId = conversationId_3)
        val notification4 = exampleNotificationFrontendModel(conversationId = conversationId_3)

        when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(customsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any()))
          .thenReturn(Future.successful(Seq(notification1, notification2, notification3, notification4)))

        controller.displayPage(getRequest).futureValue

        val viewArguments: Seq[(Submission, Seq[NotificationFrontendModel])] = captureViewArguments()

        val submissions: Seq[Submission] = viewArguments.map(_._1)
        val notifications: Seq[Seq[NotificationFrontendModel]] = viewArguments.map(_._2)
        submissions mustBe Seq(submission3, submission2, submission1)
        notifications mustBe Seq(Seq(notification4, notification3), Seq(notification2), Seq(notification1))
      }
    }
  }

  private def captureViewArguments(): Seq[(Submission, Seq[NotificationFrontendModel])] = {
    val captor: ArgumentCaptor[Seq[(Submission, Seq[NotificationFrontendModel])]] =
      ArgumentCaptor.forClass(classOf[Seq[(Submission, Seq[NotificationFrontendModel])]])
    verify(submissionsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

}
