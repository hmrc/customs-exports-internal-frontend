/*
 * Copyright 2023 HM Revenue & Customs
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

package views.summary

import base.Injector
import models.cache.ShutMucrAnswers
import views.ViewSpec
import views.html.summary.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val page = instanceOf[shut_mucr_summary]
  private val shutMucr = "some-mucr"

  "View" should {
    "render title" in {
      page(shutMucr).getTitle must containMessage("shutMucr.summary.title")
    }

    "render MUCR type in table row" in {
      page(shutMucr).getElementsByClass("govuk-summary-list__key").text() mustBe messages("shutMucr.summary.type")
    }

    "render correct mucr" in {
      page(shutMucr).getElementsByClass("govuk-summary-list__value").text() mustBe shutMucr
    }

    "render correct submit button" in {
      val submitButton = page(shutMucr).getElementsByClass("govuk-button").first()

      submitButton.text() mustBe messages("site.confirmAndSubmit")
    }
  }

}
