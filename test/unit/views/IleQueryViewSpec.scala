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
import forms.IleQueryForm
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query

class IleQueryViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query]
  private val view = page(IleQueryForm.form)

  "Ile Query page" should {

    "render title" in {

      view.getTitle must containMessage("ileQuery.title")
    }

    "render page header" in {

      view.getElementsByClass("govuk-label--xl").first().text() mustBe messages("ileQuery.title")
    }

    "render error summary" when {

      "no errors" in {

        val govukErrorSummary: Element = view.getElementsByClass("govuk-error-summary__title").first()

        Option(govukErrorSummary) mustBe None
      }

      "some errors" in {

        val errorView = page(IleQueryForm.form.withError("error", "error.required"))

        val govukErrorSummary = errorView.getElementsByClass("govuk-error-summary__title").first()

        govukErrorSummary.text() mustBe messages("error.summary.title")
      }
    }

    val govukListElement = view.getElementsByClass("govuk-list").first()

    "contains submit button" in {

      view.getElementsByClass("govuk-button").first().text() mustBe messages("site.continue")
    }

//    TODO better way to signal pending tests
//    "contains link to create empty mucr" in {
//
//      govukListElement.getElementsByClass("govuk-link").get(0).text() mustBe messages("ileQuery.link.emptyMucr")
//    }

    "contains link to view previous requests" in {

      val previousRequests = govukListElement.getElementsByClass("govuk-link").get(0)

      previousRequests.text() mustBe messages("ileQuery.link.requests")
      previousRequests must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
    }
  }
}
