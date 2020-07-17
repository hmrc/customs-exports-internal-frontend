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
import forms.ArrivalDetails
import forms.common.{Date, Time}
import models.cache.ArrivalAnswers
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import views.html.summary.arrival_summary_page
import views.{ViewDates, ViewSpec}

class ArrivalSummaryViewSpec extends ViewSpec with Injector {

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
  private val answers = ArrivalAnswers(arrivalDetails = Some(ArrivalDetails(date, time)))
  private val viewDates = new ViewDates()

  private implicit val request = journeyRequest(answers)

  private val page = instanceOf[arrival_summary_page]

  "View" should {

    "render title" in {

      page(answers).getTitle must containMessage("summary.arrival.title")
    }

    "render heading" in {

      page(answers).getElementById("title") must containMessage("summary.arrival.title")
    }

    "render back button" in {

      val backButton = page(answers).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.LocationController.displayPage())
    }

    "render sub-headers for summary sections" in {

      val summaryContent = contentAsString(page(answers))

      summaryContent must include(messages("summary.consignmentDetails"))
      summaryContent must include(messages("arrivalDetails.title"))
      summaryContent must include(messages("location.title"))
    }

    "render formatted arrivate date" in {
      val view = page(answers)

      view.getElementsByClass("govuk-summary-list__key").get(2) must containMessage("summary.arrival.date")
      view.getElementsByClass("govuk-summary-list__value").get(2).text mustBe viewDates.formatDate(date.date)
    }
  }

}
