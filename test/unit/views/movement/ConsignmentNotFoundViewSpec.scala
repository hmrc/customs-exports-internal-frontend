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

package views.movement

import base.Injector
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.consignment_not_found_page

class ConsignmentNotFoundViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[consignment_not_found_page]
  private val view = page("SOME_UCR")

  "Consignment Not Found page" should {

    "render title" in {

      view.getTitle must containMessage("ileQueryResponse.ucrNotFound.title")
    }

    "render page header" in {

      view.getElementsByClass("govuk-heading-xl").first().text() mustBe messages("ileQueryResponse.ucrNotFound.title")
    }

    "render error message" in {

      view.getElementsByClass("govuk-body").first().text() mustBe messages("ileQueryResponse.ucrNotFound.message", "SOME_UCR")
    }

    "render back button" in {
      val backButton = view.getElementsByClass("govuk-back-link").first()

      backButton.text() mustBe messages("site.back")
      backButton.attr("href") mustBe controllers.routes.ViewSubmissionsController.displayPage().url
    }
  }
}
