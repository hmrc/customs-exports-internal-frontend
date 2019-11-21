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

package views.movement

import controllers.movements.routes
import forms.GoodsDeparted
import models.cache.DepartureAnswers
import views.ViewSpec
import views.html.goods_departed

class GoodsDepartedViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(DepartureAnswers())

  private val goodsDepartedPage = new goods_departed(main_template)

  "GoodsDeparted view" should {

    "display title" in {

      goodsDepartedPage(GoodsDeparted.form).getTitle must containMessage("goodsDeparted.title")
    }

    "display radio input" in {

      goodsDepartedPage(GoodsDeparted.form).getElementById("title") must containMessage("goodsDeparted.header")
      goodsDepartedPage(GoodsDeparted.form).getElementById("outOfTheUk-label") must containMessage("goodsDeparted.departureLocation.outOfTheUk")
      goodsDepartedPage(GoodsDeparted.form).getElementById("backIntoTheUk-label") must containMessage("goodsDeparted.departureLocation.backIntoTheUk")
    }

    "display back button" in {

      val backButton = goodsDepartedPage(GoodsDeparted.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(routes.LocationController.displayPage())
    }

    "display error summary" when {
      "there are errors in the form" in {

        val page = goodsDepartedPage(GoodsDeparted.form.withError("departureLocation", "goodsDeparted.departureLocation.error.empty"))

        page.getErrorSummary mustBe defined
        page.getElementById("departureLocation-error") must haveHref("#departureLocation")
      }
    }

    "not display error summary" when {
      "there are no errors in the form" in {

        goodsDepartedPage(GoodsDeparted.form).getErrorSummary mustBe empty
      }
    }
  }

}
