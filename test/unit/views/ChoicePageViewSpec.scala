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

package views

import base.Injector
import forms.Choice
import models.UcrBlock
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.choice_page

class ChoicePageViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[choice_page]

  "Choice page page" should {

    "render title" in {
      page(Choice.form()).getTitle must containMessage("movement.choice.title")
    }

    "render all options for radio button" in {

      val choicePage = page(Choice.form())

      choicePage.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
      choicePage.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associate.label"))
      choicePage.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateDucr.label"))
      choicePage.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.shutMucr.label"))
      choicePage.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.departure.label"))
      choicePage.getElementsByAttributeValue("for", "choice-6").text() must be(messages("movement.choice.retrospectiveArrival.label"))
      choicePage.getElementsByAttributeValue("for", "choice-7").text() must be(messages("movement.choice.submissions.label"))
    }

    "render error summary" when {
      "no errors" in {
        page(Choice.form()).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(Choice.form().withError(FormError("choice", "error.required")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("choice", messages("error.required"))
      }
    }

    "render back link" when {
      "form contains ucr block" in {
        val backButton = page(Choice.form(), Some(UcrBlock("ucr", "D"))).getElementById("back-link")

        backButton must haveHref(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr"))
      }
    }

    "not render back link" when {
      "form does not contain ucr block" in {
        val backButton = Option(page(Choice.form(), None).getElementById("back-link"))

        backButton mustNot be(defined)
      }
    }

    "not render 'Shut Mucr' option" when {
      "ILE query was for a Ducr" in {
        val choicePage = page(Choice.form(), Some(UcrBlock("ducr", UcrBlock.ducrType)))

        choicePage.getElementsByClass("govuk-radios__input").size() mustBe 6

        choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "associateUCR").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "departure").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "submissions").size() must be(1)

        choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 0
      }
    }

    "render 'Shut Mucr' option" when {
      "ILE query was for a Mucr" in {
        val choicePage = page(Choice.form(), Some(UcrBlock("mucr", UcrBlock.mucrType)))

        choicePage.getElementsByClass("govuk-radios__input").size() mustBe 7

        choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "associateUCR").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "departure").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() must be(1)
        choicePage.getElementsByAttributeValue("value", "submissions").size() must be(1)

        choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 1
      }
    }
  }

}
