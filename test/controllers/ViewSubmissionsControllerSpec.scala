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

package controllers

import java.time.Instant

import base.MockExportsMovementsConnector
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.conversationId
import testdata.MovementsTestData
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.view_submissions

import scala.concurrent.ExecutionContext.Implicits.global

class ViewSubmissionsControllerSpec extends ControllerLayerSpec with MockExportsMovementsConnector with ScalaFutures {

  private val submissionsPage = mock[view_submissions]
  private val controller =
    new ViewSubmissionsController(SuccessfulAuth(), customsExportsMovementConnector, stubMessagesControllerComponents(), submissionsPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(submissionsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionsPage)

    super.afterEach()
  }

  "SubmissionController on displayPage" should {

    "return 200 (OK)" in {

      withConnectorFetchingAllSubmissions(Seq.empty)
      withConnectorFetchingNotifications(Seq.empty)

      val result = controller.displayPage(getRequest)

      status(result) mustBe OK
    }

    "call connector for all Submissions" in {

      withConnectorFetchingAllSubmissions(Seq.empty)
      withConnectorFetchingNotifications(Seq.empty)

      controller.displayPage(getRequest).futureValue

      val expectedProviderId = SuccessfulAuth().operator.pid
      verify(customsExportsMovementConnector).fetchAllSubmissions(meq(expectedProviderId))(any())
    }

    "call connector for all Notifications" in {

      val submission = MovementsTestData.exampleSubmissionFrontendModel()
      withConnectorFetchingAllSubmissions(Seq(submission))
      withConnectorFetchingNotifications(Seq.empty)

      controller.displayPage(getRequest).futureValue

      val expectedProviderId = SuccessfulAuth().operator.pid
      verify(customsExportsMovementConnector).fetchNotifications(meq(conversationId), meq(expectedProviderId))(any())
    }

    "call submissions view, passing Submissions in descending order" in {

      val submission1 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(60))
      val submission2 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(30))
      val submission3 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now())

      withConnectorFetchingAllSubmissions(Seq(submission1, submission2, submission3))
      withConnectorFetchingNotifications(Seq.empty)

      controller.displayPage(getRequest).futureValue

      val captor: ArgumentCaptor[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]] =
        ArgumentCaptor.forClass(classOf[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]])
      verify(submissionsPage).apply(captor.capture())(any(), any())

      val submissions: Seq[SubmissionFrontendModel] = captor.getValue.map(_._1)
      submissions mustBe Seq(submission3, submission2, submission1)
    }
  }

}
