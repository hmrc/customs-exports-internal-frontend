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

import forms.{ConsignmentReferenceType, ConsignmentReferences}
import models.cache.{JourneyType, RetrospectiveArrivalAnswers}
import views.ViewSpec
import views.html.movement_confirmation_page

class MovementConfirmationRetrospectiveArrivalViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(RetrospectiveArrivalAnswers())

  private val consignmentReferences = ConsignmentReferences(ConsignmentReferenceType.D, "9GB12345678")
  private val page = new movement_confirmation_page(main_template)

  "View" should {
    "render title" in {
      page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences).getTitle must containMessage(
        "movement.confirmation.RETROSPECTIVE_ARRIVE.tab.heading"
      )
    }

    "render confirmation" in {
      page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences)
        .getElementById("highlight-box-heading") must containMessage("movement.confirmation.RETROSPECTIVE_ARRIVE.heading", "DUCR", "9GB12345678")
    }

    "have back to start button" in {

      val backButton = page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences).getElementsByClass("button").first()

      backButton must containMessage("site.backToStart")
      backButton must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "have 'view requests' link" in {
      val statusInfo = page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences).getElementById("status-info")
      statusInfo.getElementsByTag("a").get(0) must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.ViewSubmissions))
    }

    "have 'next steps' link" in {
      val nextSteps = page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences).getElementById("next-steps")
      nextSteps.getElementsByTag("a").get(0) must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.AssociateUCR))
      nextSteps.getElementsByTag("a").get(1) must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.Departure))
    }
  }

}
