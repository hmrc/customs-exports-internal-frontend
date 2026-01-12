/*
 * Copyright 2024 HM Revenue & Customs
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

  private val form = Choice.form()
  private val page = instanceOf[choice_page]

  "Choice page page" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      page(form).getTitle must containMessage("movement.choice.title")
    }

    "render section header" in {
      page(form, Some(UcrBlock(ucr = "9GB123456", ucrType = Ducr)))
        .getElementById("section-header") must containMessage("movement.choice.sectionHeading", "9GB123456")
    }

    "render all options for radio button" in {
      val choicePage = page(form)

      choicePage.getElementsByAttributeValue("for", "choice").text() mustBe messages("movement.choice.arrival.label")
      choicePage.getElementsByAttributeValue("for", "choice-2").text() mustBe messages("movement.choice.associate.label")
      choicePage.getElementsByAttributeValue("for", "choice-3").text() mustBe messages("movement.choice.disassociateDucr.label")
      choicePage.getElementsByAttributeValue("for", "choice-4").text() mustBe messages("movement.choice.shutMucr.label")
      choicePage.getElementsByAttributeValue("for", "choice-5").text() mustBe messages("movement.choice.departure.label")
      choicePage.getElementsByAttributeValue("for", "choice-6").text() mustBe messages("movement.choice.retrospectiveArrival.label")
    }

    "render error summary" when {

      "no errors" in {
        page(form).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(form.withError(FormError("choice", "error.required")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("choice", messages("error.required"))
      }
    }

    "render back link and point to appropriate page" when {
      Seq(Some(true), None).foreach { chiefStatus =>
        s"form contains ucr block ${if (chiefStatus.isDefined) "from CHIEF" else ""}" in {
          val backButton = page(form, Some(UcrBlock(ucr = "ucr", ucrType = Ducr.codeValue, chiefUcr = chiefStatus))).getElementById("back-link")
          if (chiefStatus.isEmpty)
            backButton must haveHref("#")
          else
            backButton must haveHref("#")
        }
      }
    }

    "not render back link" when {
      "form does not contain ucr block" in {
        val backButton = Option(page(form, None).getElementById("back-link"))
        backButton mustNot be(defined)
      }
    }

    "not render 'Shut Mucr' option" when {
      Seq(Some(true), None).foreach { chiefStatus =>
        s"ILE query was for a Ducr ${if (chiefStatus.isDefined) "from CHIEF" else ""}" in {
          val choicePage = page(form, Some(UcrBlock(ucr = "ducr", ucrType = Ducr.codeValue, chiefUcr = chiefStatus)))

          choicePage.getElementsByClass("govuk-radios__input").size() mustBe 5

          choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe 1

          choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 0
        }

        s"ILE query was for a Ducr part ${if (chiefStatus.isDefined) "from CHIEF" else ""}" in {
          val choicePage = page(form, Some(UcrBlock(ucr = "ducr-part", ucrType = DucrPart.codeValue, chiefUcr = chiefStatus)))

          choicePage.getElementsByClass("govuk-radios__input").size() mustBe 5

          choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe 1

          choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 0
        }
      }
    }

    "render 'Shut Mucr' and 'Retrospective Arrival' options appropriately" when {
      List(Some(true), None).foreach { chiefStatus =>
        s"ILE query was for a Mucr ${if (chiefStatus.isDefined) "from CHIEF" else ""}" in {
          val choicePage = page(form, Some(UcrBlock(ucr = "mucr", ucrType = Mucr.codeValue, chiefUcr = chiefStatus)))

          choicePage.getElementsByClass("govuk-radios__input").size() mustBe (if (chiefStatus.isDefined) 5 else 6)

          choicePage.getElementsByAttributeValue("value", "arrival").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "associateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "disassociateUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "departure").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "shutMUCR").size() mustBe 1
          choicePage.getElementsByAttributeValue("value", "retrospectiveArrival").size() mustBe (if (chiefStatus.isDefined) 0 else 1)
        }
      }
    }
  }
}
