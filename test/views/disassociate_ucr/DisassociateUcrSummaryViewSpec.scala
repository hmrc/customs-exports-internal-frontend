/*
 * Copyright 2019 HM Revenue & Customs
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

package views.disassociate_ucr

import forms.{DisassociateKind, DisassociateUcr}
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociate_ucr_summary

class DisassociateUcrSummaryViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val answersDUCR = DisassociateUcr(DisassociateKind.Ducr, Some("ducr-reference"), None)
  private val answersMUCR = DisassociateUcr(DisassociateKind.Mucr, None, Some("mucr-reference"))
  private val page = new disassociate_ucr_summary(main_template)

  "View" should {
    "render title" in {
      page(answersDUCR).getTitle must containMessage("disassociate.ucr.summary.title")
      page(answersMUCR).getTitle must containMessage("disassociate.ucr.summary.title")
    }

    "render form" in {
      val form = page(answersDUCR).getForm
      form mustBe defined
      form.get must haveAttribute("action", controllers.consolidations.routes.DisassociateUCRSummaryController.submit().url)
    }

    "render back button" when {
      "ducr" in {
        val backButton = page(answersDUCR).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(controllers.consolidations.routes.DisassociateUCRController.display())
      }

      "mucr" in {
        val backButton = page(answersMUCR).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(controllers.consolidations.routes.DisassociateUCRController.display())
      }
    }

    "render type" when {
      "ducr" in {
        page(answersDUCR).getElementById("disassociate_ucr-type") must containText("DUCR")
      }

      "mucr" in {
        page(answersMUCR).getElementById("disassociate_ucr-type") must containText("MUCR")
      }
    }

    "render reference" when {
      "ducr" in {
        page(answersDUCR).getElementById("disassociate_ucr-reference") must containText("ducr-reference")
      }

      "mucr" in {
        page(answersMUCR).getElementById("disassociate_ucr-reference") must containText("mucr-reference")
      }
    }

    "render remove link" when {
      "ducr" in {
        val anchor: Element = page(answersDUCR).getElementById("disassociate_ucr-remove")
        anchor must containMessage("site.change")
        anchor must haveHref(controllers.consolidations.routes.DisassociateUCRController.display())
      }

      "mucr" in {
        val anchor = page(answersDUCR).getElementById("disassociate_ucr-remove")
        anchor must containMessage("site.change")
        anchor must haveHref(controllers.consolidations.routes.DisassociateUCRController.display())
      }
    }

    "render submit button" when {
      "ducr" in {
        val submit = page(answersDUCR).getSubmitButton
        submit mustBe defined
        submit.get must containMessage("site.confirmAndSubmit")
      }

      "mucr" in {
        val submit = page(answersMUCR).getSubmitButton
        submit mustBe defined
        submit.get must containMessage("site.confirmAndSubmit")
      }
    }
  }

}
