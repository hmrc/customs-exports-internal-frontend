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

package views

import base.Injector
import controllers.CSRFSupport
import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import models.cache.Answers
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.providerId

class ViewSpec extends WordSpec with MustMatchers with ViewTemplates with ViewMatchers with Injector with CSRFSupport {

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  private val messagesApi: MessagesApi = ViewSpec.realMessagesApi

  protected implicit def messages(implicit request: Request[_]): Messages =
    new AllMessageKeysAreMandatoryMessages(messagesApi.preferred(request))

  protected implicit def messages(key: String)(implicit request: Request[_]): String = messages(request)(key)

  protected def journeyRequest(answers: Answers) = JourneyRequest(answers, AuthenticatedRequest(Operator(providerId), FakeRequest().withCSRFToken))

  /*
    Fails the test if a view is configured with a message key that doesnt exist in the messages file
   */
  private class AllMessageKeysAreMandatoryMessages(msg: Messages) extends Messages {
    override def messages: Messages = msg.messages

    override def lang: Lang = msg.lang

    override def apply(key: String, args: Any*): String =
      if (msg.isDefinedAt(key))
        msg.apply(key, args: _*)
      else throw new AssertionError(s"Message Key is not configured for {$key}")

    override def apply(keys: Seq[String], args: Any*): String =
      if (keys.exists(key => !msg.isDefinedAt(key)))
        msg.apply(keys, args)
      else throw new AssertionError(s"Message Key is not configured for {$keys}")

    override def translate(key: String, args: Seq[Any]): Option[String] = msg.translate(key, args)

    override def isDefinedAt(key: String): Boolean = msg.isDefinedAt(key)
  }

  /*
    Implicit Utility class for retrieving common elements which are on the vast majority of pages
   */
  protected implicit class CommonElementFinder(html: Html) {
    private val document = htmlBodyOf(html)

    def getTitle: Element = document.getElementsByTag("title").first()

    def getBackButton: Option[Element] = Option(document.getElementById("link-back"))

    def getSubmitButton: Option[Element] = Option(document.getElementById("submit"))

    def getErrorSummary: Option[Element] = Option(document.getElementById("error-summary"))

    def getForm: Option[Element] = Option(document.getElementsByTag("form")).filter(!_.isEmpty).map(_.first())
  }

}

object ViewSpec extends Injector {
  val realMessagesApi = instanceOf[MessagesApi]
}
