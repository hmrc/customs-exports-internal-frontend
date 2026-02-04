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

import base.MessagesStub
import controllers.CSRFSupport
import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import models.cache.{Answers, Cache}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.providerId

class ViewSpec extends AnyWordSpec with Matchers with ViewMatchers with MessagesStub with CSRFSupport {

  val backButtonDefaultCall: Call = Call("GET", "#")

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  protected def journeyRequest(answers: Answers): JourneyRequest[?] =
    JourneyRequest(
      Cache(providerId, Some(answers), None),
      AuthenticatedRequest(Operator(providerId), FakeRequest().withFormUrlEncodedBody().withCSRFToken)
    )

  /*
    Implicit Utility class for retrieving common elements which are on the vast majority of pages
   */
  protected implicit class CommonElementFinder(html: Html) {
    private val document = htmlBodyOf(html)

    def checkBackButton(implicit request: Request[?]): Assertion = {
      val backButton = document.getElementById("back-link")
      backButton.text mustBe messages("site.back")
      backButton must haveHref(backButtonDefaultCall)
    }

    def getTitle: Element = document.getElementsByTag("title").first()

    def getSubmitButton: Option[Element] = Option(document.getElementById("submit"))

    def getErrorSummary: Option[Element] = Option(document.getElementById("error-summary"))

    def getForm: Option[Element] = Option(document.getElementsByTag("form")).filter(!_.isEmpty).map(_.first())
  }
}
