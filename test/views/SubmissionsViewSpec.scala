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

package views

import java.time.{Instant, LocalDate, ZoneOffset}

import controllers.routes
import models.UcrBlock
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.view_submissions

class SubmissionsViewSpec extends ViewSpec {

  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)
  private def createView(submissions: Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])] = Seq.empty): Html =
    new view_submissions(main_template)(submissions)

  "Submissions page" should {

    "contain title" in {

      createView().getElementById("title") must containText(messages("submissions.title"))
    }

    "contain correct table headers" in {

      val page = createView()

      page.getElementById("ucr") must containText(messages("submissions.ucr"))
      page.getElementById("ucrType") must containText(messages("submissions.submissionType"))
      page.getElementById("submissionAction") must containText(messages("submissions.submissionAction"))
      page.getElementById("dateOfRequest") must containText(messages("submissions.dateOfRequest"))
    }

    "contain correct submission data" in {

      val submission = exampleSubmissionFrontendModel(requestTimestamp = dateTime)
      val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

      val page = createView(Seq((submission, notifications)))

      page.getElementById(s"ucr-$conversationId").text() mustBe correctUcr
      page.getElementById(s"ucrType-$conversationId").text() mustBe "DUCR"
      page.getElementById(s"dateOfRequest-$conversationId").text() mustBe "31 Oct 2019 at 00:00"
      page.getElementById(s"submissionAction-$conversationId").text() mustBe messages(s"submissions.arrival")
    }

    "contain link to ViewNotifications page" when {
      "there are Notifications for the Submission" in {

        val submission = exampleSubmissionFrontendModel(requestTimestamp = dateTime)
        val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

        val page = createView(Seq((submission, notifications)))

        page.getElementById(s"ucr-$conversationId").child(0) must haveHref(routes.ViewNotificationsController.listOfNotifications(conversationId))
      }
    }

    "contain MUCR and DUCR if Submission contains both" in {

      val submission = exampleSubmissionFrontendModel(
        requestTimestamp = dateTime,
        ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "D"))
      )
      val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

      val page = createView(Seq((submission, notifications)))

      val actualUcrs = page.getElementById(s"ucr-$conversationId").text()
      actualUcrs must include(correctUcr)
      actualUcrs must include(correctUcr_2)
      val actualUcrTypes = page.getElementById(s"ucrType-$conversationId").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }
  }

}
