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

import base.Injector
import forms.{Location, SpecificDateTimeChoice}
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import views.ViewSpec
import views.html.location

class LocationViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val userDateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime)
  private val currentDateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime)

  private val page = instanceOf[location]

  "View" should {

    "render title" when {

      "used for Arrival journey" in {

        page(Location.form(), "D", None).getTitle must containMessage("location.question")
      }

      "used for Retrospective Arrival journey" in {

        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        page(Location.form(), "D", None).getTitle must containMessage("location.question")
      }

      "used for Departure journey" in {

        implicit val request = journeyRequest(DepartureAnswers())
        page(Location.form(), "D", None).getTitle must containMessage("location.question")
      }
    }

    "render heading" when {

      "used for Arrival journey" in {

        val locationPage = page(Location.form(), "D", None)

        locationPage.getTitle must containMessage("location.question")
        locationPage.getElementById("code-hint").text() mustBe messages("location.hint")
      }

      "used for Retrospective Arrival journey" in {

        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        val locationPage = page(Location.form(), "D", None)

        locationPage.getElementById("section-header") must containMessage("movement.sectionHeading", "Retrospective_arrive", "D")
        locationPage.getTitle must containMessage("location.question")
        locationPage.getElementById("code-hint") must containMessage("location.hint")
      }

      "used for Departure journey" in {

        implicit val request = journeyRequest(DepartureAnswers())
        val locationPage = page(Location.form(), "D", None)

        locationPage.getTitle must containMessage("location.question")
        locationPage.getElementById("code-hint") must containMessage("location.hint")
      }
    }

    "render back button" which {

      "links to Movement Details page" when {

        "user is on Arrival journey with user specified date-time" in {

          implicit val request = journeyRequest(ArrivalAnswers())

          val backButton = page(Location.form(), "D", Some(userDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe controllers.movements.routes.MovementDetailsController.displayPage().toString()
        }

        "user is on Departure journey with user specified date-time" in {

          implicit val request = journeyRequest(DepartureAnswers())

          val backButton = page(Location.form(), "D", Some(userDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe controllers.movements.routes.MovementDetailsController.displayPage().toString()
        }

      }

      "links to Specific Date-Time page" when {

        "user is on Arrival journey with current date-time" in {

          implicit val request = journeyRequest(ArrivalAnswers())

          val backButton = page(Location.form(), "D", Some(currentDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe controllers.movements.routes.SpecificDateTimeController.displayPage().toString()
        }

        "user is on Departure journey with current date-time" in {

          implicit val request = journeyRequest(DepartureAnswers())

          val backButton = page(Location.form(), "D", Some(currentDateTimeChoice)).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe controllers.movements.routes.SpecificDateTimeController.displayPage().toString()
        }
      }

      "links to Choice page" when {

        "user is on Retrospective Arrival journey" in {

          implicit val request = journeyRequest(RetrospectiveArrivalAnswers())

          val backButton = page(Location.form(), "D", None).getElementById("back-link")

          backButton.text() mustBe messages("site.back")
          backButton.attr("href") mustBe controllers.routes.ChoiceController.displayPage().toString()

        }
      }
    }

    "render error summary" when {

      "no errors" in {

        page(Location.form(), "D", None).getErrorSummary mustBe empty
      }

      "some errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        page(Location.form().withError("code", "error.required"), "D", None).getElementById("error-summary-title").text() mustBe messages(
          "error.summary.title"
        )
      }
    }
  }

}
