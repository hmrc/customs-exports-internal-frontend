/*
 * Copyright 2023 HM Revenue & Customs
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
import models.UcrType.{Ducr, DucrPart, Mucr}
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

    "render section header" in {

      page(Choice.form(), Some(UcrBlock(ucr = "9GB123456", ucrType = Ducr)))
        .getElementById("section-header") must containMessage("movement.choice.sectionHeading", "9GB123456")
    }

    "render all options for radio button" in {

      val choicePage = page(Choice.form())

      choicePage.getElementsByAttributeValue("for", "choice").text() mustBe messages("movement.choice.arrival.label")
      choicePage.getElementsByAttributeValue("for", "choice-2").text() mustBe messages("movement.choice.associate.label")
      choicePage.getElementsByAttributeValue("for", "choice-3").text() mustBe messages("movement.choice.disassociateDucr.label")
      choicePage.getElementsByAttributeValue("for", "choice-4").text() mustBe messages("movement.choice.shutMucr.label")
      choicePage.getElementsByAttributeValue("for", "choice-5").text() mustBe messages("movement.choice.departure.label")
      choicePage.getElementsByAttributeValue("for", "choice-6").text() mustBe messages("movement.choice.retrospectiveArrival.label")
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

        val backButton = page(Choice.form(), Some(UcrBlock(ucr = "ucr", ucrType = Ducr))).getElementById("back-link")

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

        val choicePage = page(Choice.form(), Some(UcrBlock(ucr = "ducr", ucrType = Ducr)))

        choicePage.getElementsByClass("govuk-radios__input").size() mustBe 5

        choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe 1

        choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 0
      }
    }

    "not render 'Shut Mucr' and 'Retrospective arrival` option" when {

      "ILE query was for a Ducr" in {

        val choicePage = page(Choice.form(), Some(UcrBlock(ucr = "ducr-part", ucrType = DucrPart)))

        choicePage.getElementsByClass("govuk-radios__input").size() mustBe 4

        choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe 0

        choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 0
      }
    }

    "render 'Shut Mucr' option" when {

      "ILE query was for a Mucr" in {

        val choicePage = page(Choice.form(), Some(UcrBlock(ucr = "mucr", ucrType = Mucr)))

        choicePage.getElementsByClass("govuk-radios__input").size() mustBe 6

        choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
        choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe 1

        choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 1
      }
    }
  }
}
