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

import forms.ConsignmentReferences
import models.cache.ArrivalAnswers
import views.ViewSpec
import views.html.movement_confirmation_page

class MovementConfirmationArrivalViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  val consignmentReferences = ConsignmentReferences(ConsignmentReferences.AllowedReferences.Ducr, "9GB12345678")
  private val page = new movement_confirmation_page(main_template)

  "View" should {
    "render title" in {
      page(consignmentReferences).getTitle must containMessage("movement.ARRIVE.confirmation.tab.heading")
    }

    "render confirmation" in {
      page(consignmentReferences).getElementById("highlight-box-heading") must containMessage(
        "movement.ARRIVE.confirmation.heading",
        "DUCR",
        "9GB12345678"
      )
    }

    "have back to start button" in {

      val backButton = page(consignmentReferences).getElementsByClass("button").first()

      backButton must containMessage("site.backToStart")
      backButton must haveHref(controllers.routes.ChoiceController.displayPage())
    }
  }

}
