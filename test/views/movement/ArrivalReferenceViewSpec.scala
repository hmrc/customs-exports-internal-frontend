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

import forms.ArrivalReference
import models.cache.ArrivalAnswers
import views.ViewSpec
import views.html.arrival_reference

class ArrivalReferenceViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = new arrival_reference(main_template)

  "View" should {
    "render title" in {
      page(ArrivalReference.form).getTitle must containMessage("arrivalReference.question")
    }

    "render question" in {
      page(ArrivalReference.form).getElementById("reference-label") must containMessage("arrivalReference.question")
    }

    "render back button" in {
      val backButton = page(ArrivalReference.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.ConsignmentReferencesController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(ArrivalReference.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(ArrivalReference.form.withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
