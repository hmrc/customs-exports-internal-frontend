/*
 * Copyright 2022 HM Revenue & Customs
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

package views.disassociateucr

import base.Injector
import forms.DisassociateUcr
import models.UcrType
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr_summary

class DisassociateUcrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val answersDUCR = DisassociateUcr(UcrType.Ducr, Some("ducr-reference"), None)
  private val answersMUCR = DisassociateUcr(UcrType.Mucr, None, Some("mucr-reference"))
  private val page = instanceOf[disassociate_ucr_summary]

  "View" should {
    "render title" in {
      page(answersDUCR).getTitle must containMessage("disassociate.ucr.summary.title")
      page(answersMUCR).getTitle must containMessage("disassociate.ucr.summary.title")
    }

    "render form" in {
      val form = page(answersDUCR).getForm
      form mustBe defined
      form.get must haveAttribute("action", controllers.consolidations.routes.DisassociateUcrSummaryController.submit().url)
    }

    "render back button" when {
      "ducr" in {
        val backButton = page(answersDUCR).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(controllers.routes.ChoiceController.displayPage())
      }

      "mucr" in {
        val backButton = page(answersMUCR).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(controllers.routes.ChoiceController.displayPage())
      }
    }

    "render type" when {
      "ducr" in {
        page(answersDUCR).getElementsByClass("govuk-summary-list__key").first() must containText("DUCR")
      }

      "mucr" in {
        page(answersMUCR).getElementsByClass("govuk-summary-list__key").first() must containText("MUCR")
      }
    }

    "render reference" when {
      "ducr" in {
        page(answersDUCR).getElementsByClass("govuk-summary-list__value").first() must containText("ducr-reference")
      }

      "mucr" in {
        page(answersMUCR).getElementsByClass("govuk-summary-list__value").first() must containText("mucr-reference")
      }
    }

    "render submit button" when {
      "ducr" in {
        page(answersDUCR).getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
      }

      "mucr" in {
        page(answersMUCR).getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
      }
    }
  }

}
