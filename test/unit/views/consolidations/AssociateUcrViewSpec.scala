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

import forms.AssociateKind.{Ducr, Mucr}
import forms.{AssociateUcr, MucrOptions}
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.{ViewMatchers, ViewSpec}

class AssociateUcrViewSpec extends ViewSpec with ViewMatchers {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken
  private val page = new views.html.associate_ucr(main_template)

  val mucrOptions = MucrOptions("MUCR")

  private def createView(mucr: MucrOptions, form: Form[AssociateUcr]): Html =
    page(form, mucr)

  "Associate Ucr View" when {

    "form is empty" should {

      val emptyView = createView(mucrOptions, AssociateUcr.form)

      "have 'DUCR' section" which {

        "have radio button" in {
          emptyView.getElementById("associate.ucr.ducr") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "associate.ucr.ducr").text() mustBe messages("associate.ucr.ducr")
        }

        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }
      }

      "have 'MUCR' section" which {

        "have radio button" in {
          emptyView.getElementById("associate.ucr.mucr") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "associate.ucr.mucr").text() mustBe messages("associate.ucr.mucr")
        }

        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Continue' button on page" in {
        emptyView.getElementsByClass("button").text() mustBe messages("site.continue")
      }
    }

    "form contains 'MUCR' with value" should {

      val mucrView = createView(mucrOptions, AssociateUcr.form.fill(AssociateUcr(Mucr, "1234")))

      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains 'DUCR' with value" should {

      val ducrView = createView(mucrOptions, AssociateUcr.form.fill(AssociateUcr(Ducr, "1234")))

      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }
  }
}
