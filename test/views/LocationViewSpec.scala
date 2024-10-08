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
import controllers.movements.routes.{MovementDetailsController, SpecificDateTimeController}
import controllers.routes.ChoiceController
import forms.{Location, SpecificDateTimeChoice}
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import testdata.CommonTestData.validDucr
import views.html.location

class LocationViewSpec extends ViewSpec with Injector {

  private val form = Location.form()
  private val page = instanceOf[location]

  private val userDateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime)
  private val currentDateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime)

  "View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        val view = page(form.withGlobalError("error.summary.title"), validDucr, None)
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" when {

      "used for Arrival journey" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        page(form, validDucr, None).getTitle must containMessage("location.question")
      }

      "used for Retrospective Arrival journey" in {
        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        page(form, validDucr, None).getTitle must containMessage("location.question.retrospectiveArrival")
      }

      "used for Departure journey" in {
        implicit val request = journeyRequest(DepartureAnswers())
        page(form, validDucr, None).getTitle must containMessage("location.question")
      }
    }

    "render heading" when {

      "used for Arrival journey" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        val locationPage = page(form, validDucr, None)

        locationPage.getElementById("section-header") must containMessage("movement.sectionHeading.ARRIVE", validDucr)
        locationPage.getTitle must containMessage("location.question")
        locationPage.getElementById("code-hint").text() mustBe messages("location.hint")
      }

      "used for Retrospective Arrival journey" in {
        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        val locationPage = page(form, validDucr, None)

        locationPage.getElementById("section-header") must containMessage("movement.sectionHeading.RETROSPECTIVE_ARRIVE", validDucr)
        locationPage.getTitle must containMessage("location.question.retrospectiveArrival")
        locationPage.getElementById("code-hint") must containMessage("location.hint")
      }

      "used for Departure journey" in {
        implicit val request = journeyRequest(DepartureAnswers())
        val locationPage = page(form, validDucr, None)

        locationPage.getElementById("section-header") must containMessage("movement.sectionHeading.DEPART", validDucr)
        locationPage.getTitle must containMessage("location.question")
        locationPage.getElementById("code-hint") must containMessage("location.hint")
      }
    }

    "render back button" which {

      "links to Movement Details page" when {

        "user is on Arrival journey with user specified date-time" in {
          implicit val request = journeyRequest(ArrivalAnswers())

          val backButton = page(form, validDucr, Some(userDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe MovementDetailsController.displayPage.toString
        }

        "user is on Departure journey with user specified date-time" in {
          implicit val request = journeyRequest(DepartureAnswers())

          val backButton = page(form, validDucr, Some(userDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe MovementDetailsController.displayPage.toString
        }
      }

      "links to Specific Date-Time page" when {

        "user is on Arrival journey with current date-time" in {
          implicit val request = journeyRequest(ArrivalAnswers())

          val backButton = page(form, validDucr, Some(currentDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe SpecificDateTimeController.displayPage.toString()
        }

        "user is on Departure journey with current date-time" in {
          implicit val request = journeyRequest(DepartureAnswers())

          val backButton = page(form, validDucr, Some(currentDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe SpecificDateTimeController.displayPage.toString()
        }
      }

      "links to Choice page" when {
        "user is on Retrospective Arrival journey" in {
          implicit val request = journeyRequest(RetrospectiveArrivalAnswers())

          val backButton = page(form, validDucr, None).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe ChoiceController.displayPage.toString

        }
      }
    }

    "render error summary" when {

      "no errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())

        page(form, validDucr, None).getErrorSummary mustBe empty
      }

      "some errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())

        val view = page(form.withError("code", "error.required"), validDucr, None)
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }
}
