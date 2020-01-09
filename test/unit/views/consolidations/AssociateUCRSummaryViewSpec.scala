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

import forms.AssociateKind.Ducr
import forms.AssociateUcr
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec

class AssociateUCRSummaryViewSpec extends ViewSpec {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken
  private val page = new views.html.associate_ucr_summary(main_template)

  private def createView(mucr: String, ducr: String): Html =
    page(AssociateUcr(Ducr, ducr), mucr)

  "Associate UCR Confirmation View" should {

    val view = createView("MUCR", "DUCR")

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Change' link on page for associate ucr" in {
      view.getElementById("associate_ducr-change") must containText(messages("site.change"))
      view.getElementById("associate_ducr-change") must haveHref(controllers.consolidations.routes.AssociateUCRController.displayPage())
    }

    "display 'Change' link on the page for mucr" in {

      view.getElementById("associate_mucr-change") must containText(messages("site.change"))
      view.getElementById("associate_mucr-change") must haveHref(controllers.consolidations.routes.MucrOptionsController.displayPage())
    }

    "display 'Reference' link on page" in {
      view.getElementById("associate_ucr-reference") must containText("DUCR")
    }

    "display mucr type on the page" in {

      view.getElementById("mucr-type") must containText(messages("associate.ucr.summary.kind.mucr"))
    }
  }
}
