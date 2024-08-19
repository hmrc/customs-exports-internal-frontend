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

package views.components.summary

import controllers.movements.routes.GoodsDepartedController
import forms.GoodsDeparted
import forms.GoodsDeparted.DepartureLocation
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import base.Injector
import views.html.components.summary.goods_departed_summary_list
import views.{ViewMatchers, ViewSpec}

class GoodsDepartedSummaryListViewSpec extends ViewSpec with ViewMatchers with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val goodsDepartedList = instanceOf[goods_departed_summary_list]

  private val goodsDeparted = GoodsDeparted(DepartureLocation.OutOfTheUk)

  "GoodsDepartedSummaryList" should {

    "have heading" in {
      goodsDepartedList(Some(goodsDeparted)).getElementsByClass("govuk-heading-m").first() must containMessage("goodsDeparted.title")
    }

    "have 'Where are the goods going?' row with 'Change' link" in {
      val goodsDepartedRow = goodsDepartedList(Some(goodsDeparted)).getElementsByClass("govuk-summary-list__row").get(0)

      goodsDepartedRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("goodsDeparted.header")
      goodsDepartedRow.getElementsByClass("govuk-summary-list__value").first() must containMessage("goodsDeparted.departureLocation.outOfTheUk")

      val changeLinkElement = goodsDepartedRow.getElementsByClass("govuk-link").first()
      changeLinkElement must haveHref(GoodsDepartedController.displayPage)
      changeLinkElement must containMessage("summary.goodsDeparted.change")
    }
  }
}
