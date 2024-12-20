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
import forms.common.{Date, Time}
import forms.{ArrivalDetails, ConsignmentReferenceType, ConsignmentReferences}
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import testdata.CommonTestData.correctUcr
import testdata.MovementsTestData
import views.html.arrival_details

import java.text.DecimalFormat
import java.time.{LocalDate, LocalTime}
import scala.jdk.CollectionConverters.IterableHasAsScala

class ArrivalDetailsViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[_] = journeyRequest(ArrivalAnswers())
  private val movementDetails = MovementsTestData.movementDetails

  private val form = movementDetails.arrivalForm
  private val page = instanceOf[arrival_details]

  private val consignmentReferences = ConsignmentReferences(ConsignmentReferenceType.D, referenceValue = correctUcr)
  private def createView(form: Form[ArrivalDetails]): Html = page(form, Some(consignmentReferences))(request, messages)

  private def convertIntoTwoDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("00")
    formatter.format(input.toLong)
  }

  private def convertIntoFourDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("0000")
    formatter.format(input.toLong)
  }

  "ArrivalDetails View" when {

    "the page has errors" should {
      "have the page's title prefixed with 'Error:'" in {
        val view = createView(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "provided with empty form" should {
      val view = createView(form)

      "have title" in {
        view.getTitle must containMessage("arrivalDetails.header")
      }

      "render the back button" in {
        view.checkBackButton
      }

      "have section header" in {
        view.getElementById("section-header") must containMessage("arrivalDetails.sectionHeading", consignmentReferences.referenceValue)
      }

      "have heading" in {
        view.getElementById("title") must containMessage("arrivalDetails.header")
      }

      "have date section" which {

        "contains label" in {
          view.getElementsByTag("legend").asScala.exists { elem =>
            elem.text() == messages("arrivalDetails.date.question")
          }
        }

        "contains hint" in {
          view.getElementById("dateOfArrival-hint") must containMessage("arrivalDetails.date.hint")
        }

        "contains input for day" in {
          view.getElementsByAttributeValue("for", "dateOfArrival_day").first() must containMessage("date.day")
          view.getElementById("dateOfArrival_day").`val`() mustBe empty
        }

        "contains input for month" in {
          view.getElementsByAttributeValue("for", "dateOfArrival_month").first() must containMessage("date.month")
          view.getElementById("dateOfArrival_month").`val`() mustBe empty
        }

        "contains input for year" in {
          view.getElementsByAttributeValue("for", "dateOfArrival_year").first() must containMessage("date.year")
          view.getElementById("dateOfArrival_year").`val`() mustBe empty
        }
      }

      "have time section" which {

        "contains label" in {
          view.getElementsByTag("legend").asScala.exists { elem =>
            elem.text() == messages("arrivalDetails.time.question")
          }
        }

        "contains hint" in {
          view.getElementById("timeOfArrival-hint") must containMessage("arrivalDetails.time.hint")
        }

        "contains input for hour" in {
          view.getElementsByAttributeValue("for", "timeOfArrival_hour").first() must containMessage("time.hour")
          view.getElementById("timeOfArrival_hour").`val`() mustBe empty
        }

        "contains input for minute" in {
          view.getElementsByAttributeValue("for", "timeOfArrival_minute").first() must containMessage("time.minute")
          view.getElementById("timeOfArrival_minute").`val`() mustBe empty
        }
      }

      "have 'Continue' button" in {
        view.getElementsByClass("govuk-button").first() must containMessage("site.continue")
      }
    }

    "provided with form containing data" should {
      val date = LocalDate.now().minusDays(1)
      val time = LocalTime.of(1, 2)
      val viewWithData = createView(form.fill(ArrivalDetails(Date(date), Time(time))))

      "have value in day field" in {
        viewWithData.getElementById("dateOfArrival_day").`val`() mustBe convertIntoTwoDigitFormat(date.getDayOfMonth)
      }

      "have value in month field" in {
        viewWithData.getElementById("dateOfArrival_month").`val`() mustBe convertIntoTwoDigitFormat(date.getMonthValue)
      }

      "have value in year field" in {
        viewWithData.getElementById("dateOfArrival_year").`val`() mustBe convertIntoFourDigitFormat(date.getYear)
      }

      "have value in hour field" in {
        viewWithData.getElementById("timeOfArrival_hour").`val`() mustBe time.getHour.toString
      }

      "have value in minute field" in {
        viewWithData.getElementById("timeOfArrival_minute").`val`() mustBe convertIntoTwoDigitFormat(time.getMinute)
      }
    }

    "provided with Date error" should {
      val viewWithDateError: Document = createView(form.withError("dateOfArrival", "date.error.invalid"))

      "have error summary" in {
        viewWithDateError must haveGovUkGlobalErrorSummary
      }

      "have field error for Date" in {
        viewWithDateError must haveGovUkFieldError("dateOfArrival", messages("date.error.invalid"))
        viewWithDateError must haveGovUkGlobalErrorLink("#dateOfArrival.day", messages("date.error.invalid"))
      }
    }

    "provided with Time error" should {
      val viewWithTimeError: Document = createView(form.withError("timeOfArrival", "time.error.invalid"))

      "have error summary" in {
        viewWithTimeError must haveGovUkGlobalErrorSummary
      }

      "have field error for Time" in {
        viewWithTimeError must haveGovUkFieldError("timeOfArrival", messages("time.error.invalid"))
      }
    }

    "provided with form level DateTime error" should {
      val viewWithDateError: Document = createView(
        form
          .withError("dateOfArrival", "arrival.details.error.overdue")
          .withError("timeOfArrival", "arrival.details.error.overdue")
      )

      "have single error in summary" in {
        viewWithDateError.getElementsByClass("govuk-list govuk-error-summary__list").text() mustBe messages("arrival.details.error.overdue")
      }
    }
  }
}
