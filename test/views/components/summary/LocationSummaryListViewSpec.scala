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

import controllers.movements.routes.LocationController
import forms.Location
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import base.Injector
import views.html.components.summary.location_summary_list
import views.{ViewMatchers, ViewSpec}

class LocationSummaryListViewSpec extends ViewSpec with ViewMatchers with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val locationList = instanceOf[location_summary_list]

  private val location = Location(code = "LocationCode")

  "LocationSummaryList" should {

    "have heading" in {
      locationList(Some(location)).getElementsByClass("govuk-heading-m").first() must containMessage("location.title")
    }

    "have Goods Location Code row with 'Change' link" in {
      val goodsLocationCodeRow = locationList(Some(location)).getElementsByClass("govuk-summary-list__row").get(0)

      goodsLocationCodeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.goodsLocation")
      goodsLocationCodeRow.getElementsByClass("govuk-summary-list__value").first().text mustBe "LocationCode"

      val changeLinkElement = goodsLocationCodeRow.getElementsByClass("govuk-link").first()
      changeLinkElement must haveHref(LocationController.displayPage)
      changeLinkElement must containMessage("summary.goodsLocation.change")
    }
  }
}
