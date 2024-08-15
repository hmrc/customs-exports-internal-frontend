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
import forms.{AssociateUcr, MucrOptions}
import models.UcrType.{Ducr, Mucr}
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.associateucr.associate_ucr
import views.{ViewMatchers, ViewSpec}

class AssociateUcrViewSpec extends ViewSpec with ViewMatchers with Injector {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val form = AssociateUcr.form
  private val page = instanceOf[associate_ucr]

  val mucrOptions = MucrOptions("MUCR")

  private def createView(mucr: MucrOptions, form: Form[AssociateUcr]): Html =
    page(form, mucr)

  "Associate Ucr View" when {

    "the page has errors" should {
      "have the page's title prefixed with 'Error:'" in {
        val view = createView(mucrOptions, form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "form is empty" should {

      val emptyView = createView(mucrOptions, form)

      "display 'Back' button" in {
        val backButton = emptyView.getBackButton

        backButton mustBe defined
        backButton.foreach { button =>
          button must containMessage("site.back")
          button must haveHref(ManageMucrController.displayPage)
        }
      }

      "have 'DUCR' section" which {

        "have radio button" in {
          emptyView.getElementById("kind") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind").text() mustBe messages("associate.ucr.ducr")
        }

        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }
      }

      "have 'MUCR' section" which {

        "have radio button" in {
          emptyView.getElementById("kind-2") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind-2").text() mustBe messages("associate.ucr.mucr")
        }

        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Continue' button on page" in {
        emptyView.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
      }
    }

    "form contains 'MUCR' with value" should {
      val mucrView = createView(mucrOptions, form.fill(AssociateUcr(Mucr, "1234")))

      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains 'DUCR' with value" should {
      val ducrView = createView(mucrOptions, form.fill(AssociateUcr(Ducr, "1234")))

      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }
  }
}
