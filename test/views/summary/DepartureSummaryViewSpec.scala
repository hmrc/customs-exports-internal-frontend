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

package views.summary

import base.Injector
import controllers.exchanges.JourneyRequest
import forms.DepartureDetails
import forms.common.{Date, Time}
import models.cache.DepartureAnswers
import play.api.test.Helpers._
import views.ViewSpec
import views.helpers.ViewDates
import views.html.summary.departure_summary_page

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

class DepartureSummaryViewSpec extends ViewSpec with Injector {

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
  private val answers = DepartureAnswers(departureDetails = Some(DepartureDetails(date, time)))

  private implicit val request: JourneyRequest[_] = journeyRequest(answers)

  private val departureSummaryPage = instanceOf[departure_summary_page]

  "View" should {

    "render title" in {
      departureSummaryPage(answers).getTitle must containMessage("summary.departure.title")
    }

    "render heading" in {
      departureSummaryPage(answers).getElementById("title") must containMessage("summary.departure.title")
    }

    "render the back button" in {
      departureSummaryPage(answers).checkBackButton
    }

    "render sub-headers for summary sections" in {
      val summaryContent = contentAsString(departureSummaryPage(answers))

      summaryContent must include(messages("summary.consignmentDetails"))
      summaryContent must include(messages("location.title"))
      summaryContent must include(messages("departureDetails.title"))
      summaryContent must include(messages("goodsDeparted.title"))
      summaryContent must include(messages("transport.title"))
    }

    "render formatted departure date and time" in {
      val view = departureSummaryPage(answers)

      view.getElementsByClass("govuk-summary-list__key").get(2) must containMessage("summary.departure.date")
      view.getElementsByClass("govuk-summary-list__value").get(2).text mustBe ViewDates.formatDate(date.date)

      view.getElementsByClass("govuk-summary-list__key").get(3) must containMessage("summary.departure.time")
      view.getElementsByClass("govuk-summary-list__value").get(3).text mustBe ViewDates.formatTime(time.time)
    }
  }
}
