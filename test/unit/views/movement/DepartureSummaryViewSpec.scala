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

package views.movement

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

import base.Injector
import forms.DepartureDetails
import forms.common.{Date, Time}
import models.cache.DepartureAnswers
import play.api.test.Helpers._
import views.html.summary.departure_summary_page
import views.{ViewDates, ViewSpec}

class DepartureSummaryViewSpec extends ViewSpec with Injector {

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
  private val answers = DepartureAnswers(departureDetails = Some(DepartureDetails(date, time)))
  private val viewDates = new ViewDates()

  private implicit val request = journeyRequest(answers)

  private val departureSummaryPage = instanceOf[departure_summary_page]

  "View" should {

    "render title" in {

      departureSummaryPage(answers).getTitle must containMessage("summary.departure.title")
    }

    "render heading" in {

      departureSummaryPage(answers).getElementById("title") must containMessage("summary.departure.title")
    }

    "render back button" in {

      val backButton = departureSummaryPage(answers).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.TransportController.displayPage())
    }

    "render sub-headers for summary sections" in {

      val summaryContent = contentAsString(departureSummaryPage(answers))

      summaryContent must include(messages("summary.consignmentDetails"))
      summaryContent must include(messages("location.title"))
      summaryContent must include(messages("departureDetails.title"))
      summaryContent must include(messages("goodsDeparted.title"))
      summaryContent must include(messages("transport.title"))
    }

    "render formatted departure date" in {
      val view = departureSummaryPage(answers)

      view.getElementsByClass("govuk-summary-list__key").get(2) must containMessage("summary.departure.date")
      view.getElementsByClass("govuk-summary-list__value").get(2).text mustBe viewDates.formatDate(date.date)
    }
  }

}
