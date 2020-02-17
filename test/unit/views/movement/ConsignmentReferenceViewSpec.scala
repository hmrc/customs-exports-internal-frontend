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
import models.cache.{ArrivalAnswers, DepartureAnswers, RetrospectiveArrivalAnswers}
import views.ViewSpec
import views.html.consignment_references

class ConsignmentReferenceViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[consignment_references]

  "View" should {

    "render title" in {

      page(ConsignmentReferences.form()).getTitle must containMessage("consignmentReferences.arrive.question")
    }

    "render heading" when {

      "used for Arrival journey" in {

        page(ConsignmentReferences.form()).getElementById("section-header") must containMessage("consignmentReferences.arrive.heading")
      }

      "used for Retrospective Arrival journey" in {

        implicit val request = journeyRequest(RetrospectiveArrivalAnswers())
        page(ConsignmentReferences.form()).getElementById("section-header") must containMessage("consignmentReferences.retrospective_arrive.heading")
      }

      "used for Departure journey" in {

        implicit val request = journeyRequest(DepartureAnswers())
        page(ConsignmentReferences.form()).getElementById("section-header") must containMessage("consignmentReferences.depart.heading")
      }
    }

    "render options" in {
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "reference").first() must containMessage(
        "consignmentReferences.reference.ducr"
      )
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "reference-2").first() must containMessage(
        "consignmentReferences.reference.mucr"
      )
    }
    "render back button" in {

      val backButton = page(ConsignmentReferences.form()).getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton.attr("href") mustBe controllers.routes.ChoiceController.displayPage().toString()
    }

    "render error summary" when {

      "no errors" in {

        page(ConsignmentReferences.form()).getErrorSummary mustBe empty
      }

      "some errors" in {
        implicit val request = journeyRequest(ArrivalAnswers())
        page(ConsignmentReferences.form().withError("reference", "error.required")).getElementById("error-summary-title").text() mustBe messages(
          "error.summary.title"
        )
      }
    }
  }

}
