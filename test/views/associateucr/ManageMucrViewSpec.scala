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

package views.associateucr

import base.Injector
import controllers.exchanges.JourneyRequest
import forms.ManageMucrChoice
import models.UcrBlock
import models.UcrType.Mucr
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.ViewSpec
import views.html.associateucr.manage_mucr

class ManageMucrViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[_] = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[manage_mucr]
  private val form: Form[ManageMucrChoice] = ManageMucrChoice.form()

  private val queryUcr = Some(UcrBlock(ucr = "mucr", ucrType = Mucr))

  "MUCR options" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), queryUcr)
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "have the correct title" in {
      page(form, queryUcr).getTitle must containMessage("manageMucr.title")
    }

    "have the correct heading" in {
      page(form, queryUcr).getElementById("section-header") must containMessage("manageMucr.heading", "mucr")
    }

    "render the correct labels" in {
      val view = page(form, queryUcr)
      view.getElementsByAttributeValue("for", "choice").first() must containMessage("manageMucr.associate.this.consignment")
      view.getElementsByAttributeValue("for", "choice-2").first() must containMessage("manageMucr.associate.other.consignment")
    }

    "render the back button" in {
      page(form, queryUcr).checkBackButton
    }

    "render error summary" when {

      "no errors" in {
        page(form, queryUcr).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(form.withError(FormError("choice", "manageMucr.input.error.empty")), queryUcr)

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("choice", messages("manageMucr.input.error.empty"))
      }
    }
  }
}
