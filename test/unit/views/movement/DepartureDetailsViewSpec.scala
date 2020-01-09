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

import forms.MovementDetails
import models.cache.ArrivalAnswers
import testdata.MovementsTestData
import views.ViewSpec
import views.html.departure_details

class DepartureDetailsViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val movementDetails = MovementsTestData.movementDetails

  private val page = new departure_details(main_template)

  "Departure View" should {
    "render title" in {
      page(movementDetails.departureForm()).getTitle must containMessage("departureDetails.header")
    }

    "render heading input with hint for date" in {
      page(movementDetails.departureForm()).getElementById("dateOfDeparture-label") must containMessage("departureDetails.date.question")
      page(movementDetails.departureForm()).getElementById("dateOfDeparture-hint") must containMessage("departureDetails.date.hint")
    }

    "render heading input with hint for time" in {
      page(movementDetails.departureForm()).getElementById("timeOfDeparture-label") must containMessage("departureDetails.time.question")
      page(movementDetails.departureForm()).getElementById("timeOfDeparture-hint") must containMessage("departureDetails.time.hint")
    }

    "render back button" in {
      val backButton = page(movementDetails.departureForm()).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.ConsignmentReferencesController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(movementDetails.departureForm()).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(movementDetails.departureForm().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }
}
