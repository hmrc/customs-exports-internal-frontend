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

package views.consolidations

import models.cache.ShutMucrAnswers
import views.ViewSpec
import views.html.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val page = new shut_mucr_summary(main_template)
  private val shutMucr = "some-mucr"

  "View" should {
    "render title" in {
      page(shutMucr).getTitle must containMessage("shutMucr.summary.title")
    }

    "render page header" in {
      page(shutMucr).getElementById("shutMucr-header") must containMessage("shutMucr.summary.header")
    }

    "render MUCR type in table row" in {
      page(shutMucr).getElementById("shutMucr-type") must containMessage("shutMucr.summary.type")
    }

    "render correct mucr" in {
      page(shutMucr).getElementById("shutMucr-mucr").text() must include("some-mucr")
    }

    "render correct change button" in {
      val changeButton = page(shutMucr).getElementById("shutMucr-change")

      changeButton must haveHref(controllers.consolidations.routes.ShutMucrController.displayPage())
      changeButton must containMessage("site.edit")
    }

    "render correct submit button" in {
      val submitButton = page(shutMucr).getElementById("submit")

      submitButton must containMessage("site.confirmAndSubmit")
    }
  }

}
