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
import forms.SpecificDateTimeChoice
import models.cache.{ArrivalAnswers, DepartureAnswers}
import play.api.data.Form
import play.twirl.api.Html
import views.html.specific_date_and_time

class SpecificDateTimeViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[specific_date_and_time]

  private val form: Form[SpecificDateTimeChoice] = SpecificDateTimeChoice.form()
  private implicit val request: JourneyRequest[_] = journeyRequest(ArrivalAnswers())

  private def createView: Html = page(form, "some-reference")

  "SpecificDateTime View on empty page" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), "some-reference")
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "display page title" in {
      createView.getElementsByTag("h1").first() must containMessage("specific.datetime.heading")
    }

    "have the correct section header for the Arrival journey" in {
      createView.getElementById("section-header") must containMessage("specific.datetime.arrive.heading", "some-reference")
    }

    "have the correct section header for the Departure journey" in {
      val departureView = page(form, "some-reference")(journeyRequest(DepartureAnswers()), messages)
      departureView.getElementById("section-header") must containMessage("specific.datetime.depart.heading", "some-reference")
    }

    "render the back button" in {
      createView.checkBackButton
    }

    "display 'Continue' button on page" in {
      createView.getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }
  }
}
