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
import forms.ConsignmentReferences
import models.cache.ArrivalAnswers
import testdata.MovementsTestData
import views.ViewSpec
import views.html.arrival_details

class ArrivalDetailsViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())
  private val movementDetails = MovementsTestData.movementDetails

  private val page = instanceOf[arrival_details]

  "Arrival View" should {
    "render title" in {
      page(movementDetails.arrivalForm(), None).getTitle must containMessage("arrivalDetails.header")
    }

    "render heading input with hint for date" in {
      page(movementDetails.arrivalForm(), None).getElementById("dateOfArrival-hint") must containMessage("arrivalDetails.date.hint")
    }

    "render heading input with hint for time" in {
      page(movementDetails.arrivalForm(), None).getElementById("timeOfArrival-hint") must containMessage("arrivalDetails.time.hint")
    }

    "render back button" in {
      val backButton = page(movementDetails.arrivalForm(), None).getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton.attr("href") mustBe controllers.movements.routes.ConsignmentReferencesController.displayPage().toString()
    }

    "render error summary" when {
      "no errors" in {
        page(movementDetails.arrivalForm(), None).getErrorSummary mustBe empty
      }

      "some errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        page(movementDetails.arrivalForm().withError("error", "error.required"), None).getElementById("error-summary-title").text() mustBe messages(
          "error.summary.title"
        )
      }
    }
  }
}
