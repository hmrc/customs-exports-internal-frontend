/*
 * Copyright 2019 HM Revenue & Customs
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
import views.ViewSpec
import views.html.arrival_details

class ArrivalDetailsViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = new arrival_details(main_template)

  "Arrival View" should {
    "render title" in {
      page(MovementDetails.arrivalForm()).getTitle must containMessage("arrivalDetails.header")
    }

    "render heading input with hint for date" in {
      page(MovementDetails.arrivalForm()).getElementById("dateOfArrival-label") must containMessage("arrivalDetails.date.question")
      page(MovementDetails.arrivalForm()).getElementById("dateOfArrival-hint") must containMessage("arrivalDetails.date.hint")
    }

    "render heading input with hint for time" in {
      page(MovementDetails.arrivalForm()).getElementById("timeOfArrival-label") must containMessage("arrivalDetails.time.question")
      page(MovementDetails.arrivalForm()).getElementById("timeOfArrival-hint") must containMessage("arrivalDetails.time.hint")
    }

    "render back button" in {
      val backButton = page(MovementDetails.arrivalForm()).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.ArrivalReferenceController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(MovementDetails.arrivalForm()).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(MovementDetails.arrivalForm().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }
}
