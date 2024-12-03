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
import forms.Location
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import play.api.data.Form
import play.twirl.api.Html
import testdata.CommonTestData.validDucr
import views.html.location

class LocationViewSpec extends ViewSpec with Injector {

  private val form = Location.form()
  private val page = instanceOf[location]

  private def createView(form: Form[Location] = form)(implicit request: JourneyRequest[_]): Html =
    page(form.withGlobalError("error.summary.title"), validDucr)

  private implicit val request: JourneyRequest[_] = journeyRequest(ArrivalAnswers())

  "View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = createView(form = form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" when {

      "used for Arrival journey" in {
        createView().getTitle must containMessage("location.question")
      }

      "used for Retrospective Arrival journey" in {
        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        createView().getTitle must containMessage("location.question.retrospectiveArrival")
      }

      "used for Departure journey" in {
        implicit val request = journeyRequest(DepartureAnswers())
        createView().getTitle must containMessage("location.question")
      }
    }

    "render heading" when {

      "used for Arrival journey" in {
        val view = createView()

        view.getElementById("section-header") must containMessage("movement.sectionHeading.ARRIVE", validDucr)
        view.getTitle must containMessage("location.question")
        view.getElementById("code-hint").text() mustBe messages("location.hint")
      }

      "used for Retrospective Arrival journey" in {
        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        val view = createView()

        view.getElementById("section-header") must containMessage("movement.sectionHeading.RETROSPECTIVE_ARRIVE", validDucr)
        view.getTitle must containMessage("location.question.retrospectiveArrival")
        view.getElementById("code-hint") must containMessage("location.hint")
      }

      "used for Departure journey" in {
        implicit val request = journeyRequest(DepartureAnswers())
        val view = createView()

        view.getElementById("section-header") must containMessage("movement.sectionHeading.DEPART", validDucr)
        view.getTitle must containMessage("location.question")
        view.getElementById("code-hint") must containMessage("location.hint")
      }
    }

    "render the back button" in {
      val backButton = createView().getElementById("back-link")
      backButton.text mustBe messages("site.back")
      backButton.attr("href") mustBe backButtonDefaultCall.url
    }

    "render error summary" when {

      "no errors" in {
        createView().getErrorSummary mustBe empty
      }

      "some errors" in {
        val view = createView(form.withError("code", "error.required"))
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }
}
