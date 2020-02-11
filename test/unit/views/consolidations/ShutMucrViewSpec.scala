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

import forms.ShutMucr
import base.Injector
import models.cache.ShutMucrAnswers
import org.jsoup.nodes.Document
import views.ViewSpec
import play.api.data.FormError
import views.html.shut_mucr

class ShutMucrViewSpec extends ViewSpec with Injector{

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val page = instanceOf[shut_mucr]

  "View" should {
    "render title" in {
      page(ShutMucr.form).getTitle must containMessage("shutMucr.title")
    }

    "render input for mucr" in {
      page(ShutMucr.form()).getElementsByAttributeValue("for", "mucr").first() must containMessage("shutMucr.title")
    }

    "render back button" in {
      val backButton = page(ShutMucr.form).getBackButton

      backButton mustBe defined
      backButton.foreach(button => {
        button must haveHref(controllers.routes.ChoiceController.displayPage())
        button must containMessage("site.back")
      })
    }

    "render error summary" when {
      "no errors" in {
        page(ShutMucr.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(ShutMucr.form.withError(FormError("mucr", "error.mucr.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("mucr", messages("error.mucr.empty"))
      }
    }
  }

}
