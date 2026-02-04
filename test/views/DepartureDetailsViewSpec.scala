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

package views

import base.Injector
import controllers.exchanges.JourneyRequest
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import testdata.MovementsTestData
import views.html.departure_details

import scala.jdk.CollectionConverters.IterableHasAsScala

class DepartureDetailsViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[?] = journeyRequest(ArrivalAnswers())

  private val movementDetails = MovementsTestData.movementDetails

  private val consignmentReferences = "M-ref"

  private val form = movementDetails.departureForm
  private val page = instanceOf[departure_details]

  private val view = page(form, consignmentReferences)

  "Departure View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), consignmentReferences)
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      view.getTitle must containMessage("departureDetails.header")
    }

    "have date section" which {

      "contains label" in {
        view.getElementsByTag("legend").asScala.exists { elem =>
          elem.text() == messages("departureDetails.date.question")
        }
      }

      "contains hint" in {
        view.getElementById("dateOfDeparture-hint") must containMessage("departureDetails.date.hint")
      }

      "contains input for day" in {
        view.getElementsByAttributeValue("for", "dateOfDeparture_day").first() must containMessage("date.day")
        view.getElementById("dateOfDeparture_day").`val`() mustBe empty
      }

      "contains input for month" in {
        view.getElementsByAttributeValue("for", "dateOfDeparture_month").first() must containMessage("date.month")
        view.getElementById("dateOfDeparture_month").`val`() mustBe empty
      }

      "contains input for year" in {
        view.getElementsByAttributeValue("for", "dateOfDeparture_year").first() must containMessage("date.year")
        view.getElementById("dateOfDeparture_year").`val`() mustBe empty
      }
    }

    "have time section" which {

      "contains label" in {
        view.getElementsByTag("legend").asScala.exists { elem =>
          elem.text() == messages("departureDetails.time.question")
        }
      }

      "contains hint" in {
        view.getElementById("timeOfDeparture-hint") must containMessage("departureDetails.time.hint")
      }

      "contains input for hour" in {
        view.getElementsByAttributeValue("for", "timeOfDeparture_hour").first() must containMessage("time.hour")
        view.getElementById("timeOfDeparture_hour").`val`() mustBe empty
      }

      "contains input for minute" in {
        view.getElementsByAttributeValue("for", "timeOfDeparture_minute").first() must containMessage("time.minute")
        view.getElementById("timeOfDeparture_minute").`val`() mustBe empty
      }
    }

    "render the back button" in {
      view.checkBackButton
    }

    "render error summary" when {
      "no errors" in {
        view.getErrorSummary mustBe empty
      }

      "some errors" in {
        val viewWithError = page(form.withError("error", "error.required"), consignmentReferences)
        viewWithError.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }

    "provided with Date error" should {
      val viewWithDateError: Document =
        page(form.withError("dateOfDeparture", "date.error.invalid"), consignmentReferences)

      "have error summary" in {
        viewWithDateError must haveGovUkGlobalErrorSummary
      }

      "have field error for Date" in {
        viewWithDateError must haveGovUkFieldError("dateOfDeparture", messages("date.error.invalid"))
        viewWithDateError must haveGovUkGlobalErrorLink("#dateOfDeparture.day", messages("date.error.invalid"))
      }
    }

    "provided with Time error" should {
      val viewWithTimeError: Document =
        page(form.withError("timeOfDeparture", "time.error.invalid"), consignmentReferences)

      "have error summary" in {
        viewWithTimeError must haveGovUkGlobalErrorSummary
      }

      "have field error for Time" in {
        viewWithTimeError must haveGovUkFieldError("timeOfDeparture", messages("time.error.invalid"))
      }
    }

    "provided with form level DateTime error" should {
      val viewWithDateError: Document = page(
        form
          .withError("dateOfDeparture", "departure.details.error.future")
          .withError("timeOfDeparture", "departure.details.error.future"),
        consignmentReferences
      )

      "have single error in summary" in {
        viewWithDateError.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe messages("departure.details.error.future")
      }
    }
  }
}
