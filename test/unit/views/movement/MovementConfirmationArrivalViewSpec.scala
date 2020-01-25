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
import forms.{ConsignmentReferenceType, ConsignmentReferences}
import models.cache.{ArrivalAnswers, JourneyType}
import views.ViewSpec
import views.html.movement_confirmation_page

class MovementConfirmationArrivalViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val consignmentReferences = ConsignmentReferences(ConsignmentReferenceType.D, "9GB12345678")
  private val page = instanceOf[movement_confirmation_page]

  "MovementConfirmationArrivalView" when {

    "View is rendered" should {

      "render title" in {

        page(JourneyType.ARRIVE, consignmentReferences).getTitle must containMessage("movement.confirmation.title.ARRIVE")
      }

      "render header" in {

        page(JourneyType.ARRIVE, consignmentReferences)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("movement.confirmation.title.ARRIVE", "DUCR", "9GB12345678")
      }

      "have 'notification timeline' link" in {
        val inset = page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences).getElementsByClass("govuk-inset-text").first()
        inset
          .getElementsByClass("govuk-link")
          .first() must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
      }

      "have 'find another consignment' link" in {
        page(JourneyType.RETROSPECTIVE_ARRIVE, consignmentReferences)
          .getElementsByClass("govuk-link")
          .get(1) must haveHref(controllers.routes.ChoiceController.displayPage())
      }
    }
  }

}
