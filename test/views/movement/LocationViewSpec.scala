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

import forms.Location
import models.cache.ArrivalAnswers
import views.ViewSpec
import views.html.location

class LocationViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = new location(main_template)

  "View" should {
    "render title" in {
      page(Location.form).getTitle must containMessage("location.question")
    }

    "render hint" in {
      page(Location.form).getElementById("code-hint") must containMessage("location.hint")
    }

    "render back button" in {
      val backButton = page(Location.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.MovementDetailsController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(Location.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(Location.form.withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
