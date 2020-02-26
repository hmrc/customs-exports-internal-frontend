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

package views.disassociateucr

import base.Injector
import forms.DisassociateUcr
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr

class DisassociateUcrViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val page = instanceOf[disassociate_ucr]

  "View" should {
    "render title" in {
      page(DisassociateUcr.form).getTitle must containMessage("disassociate.ucr.title")
    }

    "render back button" in {
      val backButton = page(DisassociateUcr.form).getGovUkBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "render form" in {
      val form = page(DisassociateUcr.form).getForm
      form mustBe defined
      form.get must haveAttribute("action", controllers.consolidations.routes.DisassociateUCRController.submit().url)
    }

    "render error summary" when {
      "no errors" in {
        page(DisassociateUcr.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(DisassociateUcr.form.withError(FormError("ducr", "error.required")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("ducr", messages("error.required"))
      }
    }
  }

}
