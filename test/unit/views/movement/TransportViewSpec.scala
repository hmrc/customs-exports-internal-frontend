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
import forms.Transport
import models.cache.ArrivalAnswers
import play.twirl.api.Html
import views.ViewSpec
import views.html.transport

class TransportViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[transport]

  private def createPage: Html = page(Transport.outOfTheUkForm, "some-reference")

  "View" should {
    "render title" in {
      createPage.getTitle must containMessage("transport.title")
    }

    "render input for mode of transport" in {
      createPage.getElementsByClass("govuk-fieldset__legend").get(0).text() must be(messages("transport.modeOfTransport.question"))

      createPage.getElementsByAttributeValue("for", "modeOfTransport").text() must be(messages("transport.modeOfTransport.1"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-2").text() must be(messages("transport.modeOfTransport.2"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-3").text() must be(messages("transport.modeOfTransport.3"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-4").text() must be(messages("transport.modeOfTransport.4"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-5").text() must be(messages("transport.modeOfTransport.5"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-6").text() must be(messages("transport.modeOfTransport.6"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-7").text() must be(messages("transport.modeOfTransport.7"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-8").text() must be(messages("transport.modeOfTransport.8"))
    }

    "render input for transport id" in {
      createPage.getElementsByAttributeValue("for", "transportId").first() must containMessage("transport.transportId.question")
      createPage.getElementById("transportId-hint") must containMessage("transport.transportId.hint")
    }

    "render input for nationality" in {
      createPage.getElementsByAttributeValue("for", "nationality").first() must containMessage("transport.nationality.question")
    }

    "render back button" in {
      val backButton = createPage.getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.GoodsDepartedController.displayPage)
    }

    "render error summary" when {
      "no errors" in {
        createPage.getErrorSummary mustBe empty
      }

      "some errors" in {
        val viewWithError = page(Transport.outOfTheUkForm.withError("error", "error.required"), "some-reference")
        viewWithError.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }
}
