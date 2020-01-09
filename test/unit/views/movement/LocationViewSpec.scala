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

import forms.Location
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import views.ViewSpec
import views.html.location

class LocationViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = new location(main_template)

  "View" should {

    "render title" when {

      "used for Arrival journey" in {

        page(Location.form()).getTitle must containMessage("location.question")
      }

      "used for Retrospective Arrival journey" in {

        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        page(Location.form()).getTitle must containMessage("location.question.retrospectiveArrival")
      }

      "used for Departure journey" in {

        implicit val request = journeyRequest(DepartureAnswers())
        page(Location.form()).getTitle must containMessage("location.question")
      }
    }

    "render heading" when {

      "used for Arrival journey" in {

        val locationPage = page(Location.form())

        locationPage.getElementById("title") must containMessage("location.question")
        locationPage.getElementById("hint") must containMessage("location.hint")
      }

      "used for Retrospective Arrival journey" in {

        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        val locationPage = page(Location.form())

        locationPage.getElementById("section-header") must containMessage("location.sectionHeader.retrospectiveArrival")
        locationPage.getElementById("title") must containMessage("location.question.retrospectiveArrival")
        locationPage.getElementById("hint") must containMessage("location.hint")
      }

      "used for Departure journey" in {

        implicit val request = journeyRequest(DepartureAnswers())
        val locationPage = page(Location.form())

        locationPage.getElementById("title") must containMessage("location.question")
        locationPage.getElementById("hint") must containMessage("location.hint")
      }
    }

    "render back button" which {

      "links to Movement Details page" when {

        "user is on Arrival journey" in {

          implicit val request = journeyRequest(ArrivalAnswers())

          val backButton = page(Location.form()).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(controllers.movements.routes.MovementDetailsController.displayPage())
        }

        "user is on Departure journey" in {

          implicit val request = journeyRequest(DepartureAnswers())

          val backButton = page(Location.form()).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(controllers.movements.routes.MovementDetailsController.displayPage())
        }
      }

      "links to Consignment References page" when {

        "user is on Retrospective Arrival journey" in {

          implicit val request = journeyRequest(RetrospectiveArrivalAnswers())

          val backButton = page(Location.form()).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(controllers.movements.routes.ConsignmentReferencesController.displayPage())

        }
      }
    }

    "render error summary" when {

      "no errors" in {

        page(Location.form()).getErrorSummary mustBe empty
      }

      "some errors" in {

        page(Location.form().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
