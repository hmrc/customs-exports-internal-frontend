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

package views

import base.Injector
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData
import testdata.CommonTestData.aNotificationElement
import views.html.view_notifications

class ViewNotificationsViewSpec extends ViewSpec with Injector {

  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val page = instanceOf[view_notifications]

  private val submissionUcr = ("MUCR", CommonTestData.correctUcr)

  "Notification page" should {

    "contain the expected title" in {
      page(submissionUcr, Seq.empty).getTitle must containText(messages("notifications.title"))
    }

    "contain the expected header" in {
      page(submissionUcr, Seq.empty).getElementById("title").text mustBe messages("notifications.title")
    }

    "contain the expected caption" in {
      val caption = page(submissionUcr, Seq.empty).getElementsByClass("govuk-caption-l").get(0)
      caption.text mustBe s"MUCR: ${CommonTestData.correctUcr}"
    }

    "not contain a timeline when there are no timeline events" in {
      page(submissionUcr, Seq.empty).getElementsByClass("hmrc-timeline").size mustBe 0
    }

    "contain a timeline with the expected timeline event" in {
      verifyTimeline(aNotificationElement)
    }

    "contain a timeline with the timeline events in the expected order" in {
      verifyTimeline(aNotificationElement, aNotificationElement.copy("TITLE1", "TIMESTAMP1", Html("<span>CONTENT1</span>")))
    }
  }

  def verifyTimeline(notificationElements: NotificationsPageSingleElement*): Unit = {
    val view = page(submissionUcr, Seq(notificationElements: _*))
    val timeline = view.getElementsByClass("hmrc-timeline")
    timeline.size mustBe 1

    val events = timeline.get(0).getElementsByClass("hmrc-timeline__event")
    events.size mustBe notificationElements.size

    for (ix <- 0 to notificationElements.size - 1) {
      val event = events.get(ix)
      event.tagName mustBe "li"

      val data = event.children
      data.size mustBe 3

      val notificationElement = notificationElements(ix)

      val title = data.get(0)
      title.tagName mustBe "h2"
      title.className mustBe "hmrc-timeline__event-title govuk-heading"
      title.text mustBe notificationElement.title

      val timestamp = data.get(1)
      timestamp.tagName mustBe "time"
      timestamp.className mustBe "hmrc-timeline__event-meta govuk-body"
      timestamp.text mustBe notificationElement.timestampInfo

      val content = data.get(2)
      content.tagName mustBe "div"
      content.className mustBe "hmrc-timeline__event-content"
      content.text mustBe notificationElement.content.text()
    }
  }
}
