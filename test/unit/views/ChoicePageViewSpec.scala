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

package views

import forms.Choice
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.choice_page

class ChoicePageViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = new choice_page(main_template)

  "Choice page page" should {

    "render title" in {
      page(Choice.form()).getTitle must containMessage("movement.choice.title")
    }

    "render all options for radio button" in {

      val choicePage = page(Choice.form())

      choicePage.getElementById("arrival-label") must containMessage("movement.choice.arrival.label")
      choicePage.getElementById("associate-label") must containMessage("movement.choice.associate.label")
      choicePage.getElementById("disassociate-label") must containMessage("movement.choice.disassociateDucr.label")
      choicePage.getElementById("shut_mucr-label") must containMessage("movement.choice.shutMucr.label")
      choicePage.getElementById("departure-label") must containMessage("movement.choice.departure.label")
      choicePage.getElementById("retrospectiveArrival-label") must containMessage("movement.choice.retrospectiveArrival.label")
      choicePage.getElementById("submissions-label") must containMessage("movement.choice.submissions.label")
    }

    "render error summary" when {
      "no errors" in {
        page(Choice.form()).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(Choice.form().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
