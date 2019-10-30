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

import forms.Transport
import models.cache.ArrivalAnswers
import views.ViewSpec
import views.html.transport

class TransportViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = new transport(main_template)

  "View" should {
    "render title" in {
      page(Transport.form).getTitle must containMessage("transport.title")
    }

    "render input for mode of transport" in {
      page(Transport.form).getElementById("modeOfTransport-label") must containMessage("transport.modeOfTransport.question")
      for (i <- 1 to 8) {
        page(Transport.form).getElementById(s"$i-label") must containMessage(s"transport.modeOfTransport.$i")
      }
    }

    "render input for nationality" in {
      page(Transport.form).getElementById("nationality-label") must containMessage("transport.nationality.question")
      page(Transport.form).getElementById("nationality-hint") must containMessage("transport.nationality.hint")
    }

    "render back button" in {
      val backButton = page(Transport.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.LocationController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(Transport.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(Transport.form.withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
