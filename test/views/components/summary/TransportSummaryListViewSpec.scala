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

import controllers.movements.routes.TransportController
import forms.Transport
import forms.Transport.ModesOfTransport
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import base.Injector
import views.html.components.summary.transport_summary_list
import views.{ViewMatchers, ViewSpec}

class TransportSummaryListViewSpec extends ViewSpec with ViewMatchers with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val transportList = instanceOf[transport_summary_list]

  private val transport = Transport(modeOfTransport = Some(ModesOfTransport.Sea), nationality = Some("GB"), transportId = Some("transport-id"))

  "TransportSummaryList" should {

    "have heading" in {
      transportList(Some(transport)).getElementsByClass("govuk-heading-m").first() must containMessage("transport.title")
    }

    "have Transport Type row with 'Change' link" in {
      val transportTypeRow = transportList(Some(transport)).getElementsByClass("govuk-summary-list__row").get(0)

      transportTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.modeOfTransport")
      transportTypeRow.getElementsByClass("govuk-summary-list__value").first() must containMessage("transport.modeOfTransport.1")

      val changeLinkElement = transportTypeRow.getElementsByClass("govuk-link").first()
      changeLinkElement must haveHref(TransportController.displayPage)
      changeLinkElement must containMessage("summary.modeOfTransport.change")
    }

    "have Transport ID row with 'Change' link" in {
      val transportTypeRow = transportList(Some(transport)).getElementsByClass("govuk-summary-list__row").get(1)

      transportTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.transportId")
      transportTypeRow.getElementsByClass("govuk-summary-list__value").first().text mustBe "transport-id"

      val changeLinkElement = transportTypeRow.getElementsByClass("govuk-link").first()
      changeLinkElement must haveHref(TransportController.displayPage)
      changeLinkElement must containMessage("summary.transportId.change")
    }

    "have Transport Nationality row with 'Change' link" in {
      val transportTypeRow = transportList(Some(transport)).getElementsByClass("govuk-summary-list__row").get(2)

      transportTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.nationality")
      transportTypeRow.getElementsByClass("govuk-summary-list__value").first().text mustBe "United Kingdom, Great Britain, Northern Ireland - GB"

      val changeLinkElement = transportTypeRow.getElementsByClass("govuk-link").first()
      changeLinkElement must haveHref(TransportController.displayPage)
      changeLinkElement must containMessage("summary.nationality.change")
    }
  }
}
