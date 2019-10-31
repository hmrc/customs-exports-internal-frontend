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

import forms.DisassociateUcr
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociate_ucr

class DisassociateUcrViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val page = new disassociate_ucr(main_template)

  "View" should {
    "render title" in {
      page(DisassociateUcr.form).getTitle must containMessage("disassociate.ucr.title")
    }

    "render back button" in {
      val backButton = page(DisassociateUcr.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(DisassociateUcr.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(DisassociateUcr.form.withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
