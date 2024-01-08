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

package views.associateucr

import base.Injector
import controllers.consolidations.routes.ManageMucrController
import controllers.routes.ChoiceController
import forms.ManageMucrChoice._
import forms.{ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.UcrType.Mucr
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.{Document, Element}
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.twirl.api.Html
import views.ViewSpec
import views.html.associateucr.mucr_options

class MucrOptionsViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[mucr_options]

  private val queryUcr = Some(UcrBlock(ucr = "testMucr", ucrType = Mucr))
  private def createView(form: Form[MucrOptions] = MucrOptions.form, manageMucrChoice: Option[ManageMucrChoice] = None): Html =
    page(form, queryUcr, manageMucrChoice)

  "MUCR options" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = createView(MucrOptions.form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    val view = createView()

    "have the correct title" in {
      view.getTitle must containMessage("mucrOptions.title")
    }

    "have the correct heading" in {
      view.getElementById("section-header") must containMessage("mucrOptions.heading", "testMucr")
    }

    "render the correct labels and hints" in {
      view.getElementsByAttributeValue("for", "existingMucr").first() must containMessage("site.inputText.mucr.label")
      view.getElementsByAttributeValue("for", "newMucr").first() must containMessage("site.inputText.newMucr.label")
      view.getElementById("newMucr-hint") must containMessage("site.inputText.newMucr.label.hint")
    }

    "have no options selected on initial display" in {
      view.getElementById("createOrAdd") mustBe unchecked
      view.getElementById("createOrAdd-2") mustBe unchecked
    }

    "display 'Back' button" when {

      def validateBackbutton(backButton: Option[Element], call: Call): Unit = {
        backButton mustBe defined
        backButton.foreach { button =>
          button must containMessage("site.back")
          button must haveHref(call)
        }
      }

      "query Ducr" in {
        validateBackbutton(createView().getBackButton, ChoiceController.displayPage)
      }

      "query Mucr and Associate this consignment to another" in {
        validateBackbutton(
          createView(MucrOptions.form, Some(ManageMucrChoice(AssociateThisToMucr))).getBackButton,
          ManageMucrController.displayPage
        )
      }
    }

    "display 'Continue' button on page" in {
      view.getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }

    "render error summary" when {
      "no errors" in {
        view.getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = createView(MucrOptions.form.withError(FormError("createOrAdd", "mucrOptions.createAdd.value.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("createOrAdd", messages("mucrOptions.createAdd.value.empty"))
      }
    }
  }
}
