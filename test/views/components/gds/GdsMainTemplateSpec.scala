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

package views.components.gds

import base.Injector
import controllers.exchanges.JourneyRequest
import models.cache.ArrivalAnswers
import play.api.mvc.AnyContentAsEmpty
import views.ViewSpec
import views.html.consignment_not_found_page

class GdsMainTemplateSpec extends ViewSpec with Injector {

  private val page = instanceOf[consignment_not_found_page]

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  "The Main template" should {

    "have the 'Back' link placed before the main page content" in {
      val view = page("ucr")

      val backLink = "govuk-back-link"
      view.getElementsByClass(backLink).size mustBe 1
      view.getElementsByTag("main").first.getElementsByClass(backLink).size mustBe 0
    }
  }
}
