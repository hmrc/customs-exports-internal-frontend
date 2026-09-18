/*
 * Copyright 2026 HM Revenue & Customs
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
import controllers.routes.FindCdsConsignmentController
import forms.FindCdsUcr
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.validDucr
import views.html.find_cds_consignment

class FindCdsConsignmentViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val form: Form[FindCdsUcr] = FindCdsUcr.form
  private val page = instanceOf[find_cds_consignment]

  private def createView(form: Form[FindCdsUcr] = form): Html = page(form)

  "Find CDS Consignment page" when {

    "form is empty" should {
      val view: Html = createView()

      "render title" in {
        view.getTitle must containMessage("findCdsConsignment.title")
      }

      "render page header" in {
        view.getElementById("title") must containMessage("findCdsConsignment.title")
      }

      "render the back button" in {
        val backButton = view.getElementById("back-link")

        backButton.text mustBe messages("site.back")
        backButton must haveHref(backButtonDefaultCall)
      }

      "render form posting to the submit endpoint" in {
        view.getElementsByTag("form").first must haveAttribute("action", FindCdsConsignmentController.submitCdsConsignment.url)
      }

      "render 'ucr' input with label" in {
        view.getElementsByAttributeValue("for", "ucr").first must containMessage("findCdsConsignment.ucr")
        view.getElementById("ucr") must haveTag("input")
      }

      "render 'ucr' input as empty" in {
        view.getElementById("ucr") must haveAttribute("value", "")
      }

      "not render error summary" in {
        view.getElementsByClass("govuk-error-summary") must haveSize(0)
      }

      "render 'Continue' button" in {
        view.getElementsByClass("govuk-button").first must containMessage("site.continue")
      }
    }

    "form is filled" should {
      val view: Html = createView(form.fill(FindCdsUcr(validDucr)))

      "render 'ucr' input with the provided value" in {
        view.getElementById("ucr") must haveAttribute("value", validDucr)
      }
    }

    "form has 'ucr' empty error" should {
      val view: Html = createView(form.withError(FormError("ucr", "ileQuery.ucr.empty")))

      "have the page's title prefixed with 'Error:'" in {
        view.getTitle.text must startWith("Error: ")
      }

      "render error summary" in {
        htmlBodyOf(view) must haveGovUkGlobalErrorLink("#ucr", messages("ileQuery.ucr.empty"))
      }

      "render field error" in {
        htmlBodyOf(view) must haveGovUkFieldError("ucr", messages("ileQuery.ucr.empty"))
      }
    }

    "form has 'ucr' incorrect error" should {
      val view: Html = createView(form.withError(FormError("ucr", "ileQuery.ucr.incorrect")))

      "render error summary" in {
        htmlBodyOf(view) must haveGovUkGlobalErrorLink("#ucr", messages("ileQuery.ucr.incorrect"))
      }

      "render field error" in {
        htmlBodyOf(view) must haveGovUkFieldError("ucr", messages("ileQuery.ucr.incorrect"))
      }
    }
  }
}
