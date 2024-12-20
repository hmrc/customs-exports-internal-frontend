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
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import testdata.CommonTestData.correctUcr
import views.html.ile_query_timeout

class IleQueryTimeoutViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_timeout]

  "ILE Query Timeout page" should {

    "display title" in {
      page(correctUcr).getTitle must containMessage("ileQueryResponse.timeout.title")
    }

    "render the back button" in {
      page(correctUcr).checkBackButton
    }

    "display heading" in {
      page(correctUcr).getElementsByClass("govuk-heading-xl").first() must containMessage("ileQueryResponse.timeout.heading")
    }

    "display queried UCR" in {
      page(correctUcr).getElementsByClass("govuk-body-l").first() must containText(correctUcr)
    }

    "display information paragraph" in {
      page(correctUcr).getElementsByClass("govuk-body-l").get(1) must containMessage("ileQueryResponse.timeout.message")
    }
  }
}
