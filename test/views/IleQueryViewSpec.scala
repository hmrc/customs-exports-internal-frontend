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
import controllers.routes.ViewSubmissionsController
import forms.CdsOrChiefChoiceForm
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable
import views.html.ile_query

class IleQueryViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val form = CdsOrChiefChoiceForm.form
  private val page = instanceOf[ile_query]

  private val view: Appendable = page(form)

  "Ile Query page" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      view.getTitle must containMessage("ileQuery.title")
    }

    "render page header" in {
      view.getElementsByClass("govuk-heading-xl").first.text mustBe messages("ileQuery.title")
    }

    "render error summary" when {

      "no errors" in {
        val govukErrorSummary: Element = view.getElementsByClass("govuk-error-summary__title").first
        Option(govukErrorSummary) mustBe None
      }

      "some errors" in {
        val errorView = page(form.withError("error", "error.required"))
        val govukErrorSummary = errorView.getElementsByClass("govuk-error-summary__title").first
        govukErrorSummary.text mustBe messages("error.summary.title")
      }
    }

    "contain explanatory accompanying text" in {
      view.getElementsByClass("govuk-body").first.text mustBe messages("ileQuery.paragraph")
    }

    "contain radios for CDS or CHIEF with conditional HTML" in {
      val radioItems = view.getElementsByClass("govuk-radios__item")
      radioItems.size mustBe 2
      radioItems.first must containMessage("ileQuery.radio.1")

      radioItems.get(1) must containMessage("ileQuery.radio.2")
    }

    "contains input field" in {
      Option(view.getElementById("ucr")) mustBe defined
      view.getElementsByAttributeValue("for", "ucr").first() must containMessage("ileQuery.radio.1.text")
    }

    "contains submit button" in {
      view.getElementsByClass("govuk-button").first.text mustBe messages("site.continue")
    }

    "contains link to view previous requests" in {
      val govukListElement = view.getElementsByClass("govuk-list").first

      val previousRequests = govukListElement.getElementsByClass("govuk-link").get(0)

      previousRequests.text mustBe messages("ileQuery.link.requests")
      previousRequests must haveHref(ViewSubmissionsController.displayPage)
    }
  }
}
