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
import controllers.ileQuery.routes.FindConsignmentController
import forms.ChiefConsignment
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import views.html.manage_chief_consignment

class ChiefConsignmentViewSpec extends ViewSpec with ViewMatchers with Injector {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  private val form = ChiefConsignment.form()
  private val page = instanceOf[manage_chief_consignment]

  private def createView(form: Form[ChiefConsignment]): Html = page(form)

  "ChiefConsignment view" when {

    "the page has errors" should {
      "have the page's title prefixed with 'Error:'" in {
        val view = createView(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "provided with empty form" should {
      val view = createView(form)

      "render title" in {
        view.getTitle must containMessage("manageChiefConsignment.title")
      }

      "render heading" in {
        view.getElementById("title") must containMessage("manageChiefConsignment.title")
      }

      "render page hint" in {
        view.getElementById("page-hint") must containMessage("manageChiefConsignment.pageHint")
      }

      "render 'Back' button leading to 'Find a consignment' page" in {
        view.getBackButton mustBe defined
        view.getBackButton.get must haveHref(FindConsignmentController.displayQueryForm)
      }

      "render Option 1 & 2 headings" in {
        view.getElementsByTag("h2").get(0) must containMessage("manageChiefConsignment.mucrHeading")
        view.getElementsByTag("h2").get(1) must containMessage("manageChiefConsignment.ducrHeading")
      }

      "render 'Or' text" in {
        view.getElementById("or") must containMessage("manageChiefConsignment.or")
      }

      "render MUCR input field label" in {
        view.getElementsByAttributeValue("for", "mucr").first() must containMessage("manageChiefConsignment.mucr")
      }

      "render MUCR input field hint" in {
        view.getElementById("mucr-hint") must containMessage("manageChiefConsignment.mucr.hint")
      }

      "render DUCR input field label" in {
        view.getElementsByAttributeValue("for", "ducr").first() must containMessage("manageChiefConsignment.ducr")
      }

      "render DUCR input field hint" in {
        view.getElementById("ducr-hint") must containMessage("manageChiefConsignment.ducr.hint")
      }

      "render empty DUCR input field" in {
        view.getElementById("ducr").`val`() mustBe empty
      }

      "render DUCR Part ID input field label" in {
        view.getElementsByAttributeValue("for", "ducrPartId").first() must containMessage("manageChiefConsignment.ducrPartId")
      }

      "render DUCR Part ID input field hint" in {
        view.getElementById("ducrPartId-hint") must containMessage("manageChiefConsignment.ducrPartId.hint")
      }

      "render empty DUCR Part ID input field" in {
        view.getElementById("ducrPartId").`val`() mustBe empty
      }

      "render submit button" in {
        val submitButton = view.getElementsByClass("govuk-button").first

        submitButton must containMessage("site.continue")
      }
    }

    "provided with filled form" should {
      val view = createView(form.fill(ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = Some(validDucrPartId))))

      "fill DUCR input field" in {
        view.getElementById("ducr").`val`() mustBe validDucr
      }

      "fill DUCR Part ID input field" in {
        view.getElementById("ducrPartId").`val`() mustBe validDucrPartId
      }
    }

    "provided with form containing DUCR error" should {
      val view: Document = createView(form.withError(FormError("ducr", "manageChiefConsignment.ducr.error")))

      "render error summary" in {
        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {
        view must haveGovUkFieldError("ducr", messages("manageChiefConsignment.ducr.error"))
      }
    }

    "provided with form containing DUCR Part ID error" should {
      val view: Document = createView(form.withError(FormError("ducrPartId", "manageChiefConsignment.ducrPartId.error")))

      "render error summary" in {
        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {
        view must haveGovUkFieldError("ducrPartId", messages("manageChiefConsignment.ducrPartId.error"))
      }
    }
  }
}
