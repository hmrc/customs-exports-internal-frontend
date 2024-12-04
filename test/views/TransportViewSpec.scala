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

package views

import base.Injector
import controllers.exchanges.JourneyRequest
import forms.Transport
import models.cache.ArrivalAnswers
import play.twirl.api.Html
import views.html.transport

class TransportViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[_] = journeyRequest(ArrivalAnswers())

  private val form = Transport.outOfTheUkForm
  private val page = instanceOf[transport]

  private def createView: Html = page(form, "some-reference")

  "View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), "some-reference")
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      createView.getTitle must containMessage("transport.title")
    }

    "render input for mode of transport" in {
      createView.getElementsByClass("govuk-fieldset__legend").get(0).text() must be(messages("transport.modeOfTransport.question"))

      createView.getElementsByAttributeValue("for", "modeOfTransport").text() must be(messages("transport.modeOfTransport.1"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-2").text() must be(messages("transport.modeOfTransport.2"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-3").text() must be(messages("transport.modeOfTransport.3"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-4").text() must be(messages("transport.modeOfTransport.4"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-5").text() must be(messages("transport.modeOfTransport.5"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-6").text() must be(messages("transport.modeOfTransport.6"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-7").text() must be(messages("transport.modeOfTransport.7"))
      createView.getElementsByAttributeValue("for", "modeOfTransport-8").text() must be(messages("transport.modeOfTransport.8"))
    }

    "render input for transport id" in {
      createView.getElementsByAttributeValue("for", "transportId").first() must containMessage("transport.transportId.question")
      createView.getElementById("transportId-hint") must containMessage("transport.transportId.hint")
    }

    "render input for nationality" in {
      createView.getElementsByAttributeValue("for", "nationality").first() must containMessage("transport.nationality.question")
    }

    "render the back button" in {
      createView.checkBackButton
    }

    "render error summary" when {
      "no errors" in {
        createView.getErrorSummary mustBe empty
      }

      "some errors" in {
        val viewWithError = page(form.withError("error", "error.required"), "some-reference")
        viewWithError.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }
}
