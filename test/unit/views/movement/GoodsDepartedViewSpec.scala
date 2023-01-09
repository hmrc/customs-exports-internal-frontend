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

package views.movement

import base.Injector
import controllers.movements.routes
import forms.GoodsDeparted
import models.cache.DepartureAnswers
import org.jsoup.nodes.Document
import play.api.data.FormError
import testdata.CommonTestData.validDucr
import views.ViewSpec
import views.html.goods_departed

class GoodsDepartedViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(DepartureAnswers())

  private val goodsDepartedPage = instanceOf[goods_departed]

  "GoodsDeparted view" should {

    "display title" in {

      goodsDepartedPage(GoodsDeparted.form, validDucr).getTitle must containMessage("goodsDeparted.title")
    }

    "display section header" in {

      val sectionHeader = goodsDepartedPage(GoodsDeparted.form, validDucr).getElementById("section-header")

      sectionHeader must containMessage("movement.sectionHeading.DEPART", validDucr)
    }

    "display radio input" in {

      val view = goodsDepartedPage(GoodsDeparted.form, validDucr)

      view.getElementsByClass("govuk-fieldset__heading").first() must containMessage("goodsDeparted.header")
      view.getElementsByAttributeValue("for", "departureLocation").text() must be(messages("goodsDeparted.departureLocation.outOfTheUk"))
      view.getElementsByAttributeValue("for", "departureLocation-2").text() must be(messages("goodsDeparted.departureLocation.backIntoTheUk"))
    }

    "display back button" in {

      val backButton = goodsDepartedPage(GoodsDeparted.form, validDucr).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(routes.LocationController.displayPage)
    }

    "display error summary" when {

      "there are errors in the form" in {

        val view: Document = goodsDepartedPage(GoodsDeparted.form.withError(FormError("departureLocation", "error.required")), validDucr)

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("departureLocation", messages("error.required"))
      }
    }

    "not display error summary" when {

      "there are no errors in the form" in {

        goodsDepartedPage(GoodsDeparted.form, validDucr).getErrorSummary mustBe empty
      }
    }
  }

}
