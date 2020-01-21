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

import forms.IleQuery
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query

class IleQueryViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = new ile_query(main_template)
  private val view = page(IleQuery.form)

  "Ile Query page" should {

    "render title" in {

      view.getTitle must containMessage("ileQuery.title")
    }

    "render page header" in {

      view.getElementById("title").text() mustBe messages("ileQuery.title")
    }

    "render error summary" when {

      "no errors" in {

        view.getErrorSummary mustBe empty
      }

      "some errors" in {
        page(IleQuery.form.withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }

    "contains submit button" in {

      view.getElementById("submit").text() mustBe messages("site.continue")
    }
  }
}
