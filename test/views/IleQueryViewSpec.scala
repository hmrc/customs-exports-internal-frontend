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
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat.Appendable
import views.html.ile_query

class IleQueryViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query]

  private val view: Appendable = page()

  "Ile Query page" should {

    "render title" in {
      view.getTitle must containMessage("ileQuery.title")
    }

    "render page header" in {
      view.getElementsByClass("govuk-heading-xl").first.text mustBe messages("ileQuery.title")
    }

    "contain explanatory accompanying text" in {
      view.getElementsByClass("govuk-body").first.text mustBe messages("ileQuery.paragraph")
    }

    "contains link to view previous requests" in {
      val previousRequests = view.getElementsByClass("govuk-link").get(2)

      previousRequests.text mustBe messages("ileQuery.link.requests")
      previousRequests must haveHref(ViewSubmissionsController.displayPage)
    }
  }
}
