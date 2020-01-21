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

package config

import base.UnitSpec
import controllers.CSRFSupport
import models.ReturnToStartException
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.http.ApplicationException
import views.html.error

import scala.concurrent.Future

class ErrorHandlerSpec extends UnitSpec with BeforeAndAfterEach with CSRFSupport {

  private val errorTemplate = mock[error]
  private val handler = new ErrorHandler(stubMessagesApi(), errorTemplate)
  private val fakeRequest = FakeRequest("", "").withCSRFToken

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(errorTemplate.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(errorTemplate)
    super.afterEach()
  }

  "Error handler" should {

    "display error template" in {

      handler.standardErrorTemplate("title", "heading", "message")(fakeRequest)

      verify(errorTemplate).apply(meq("title"), meq("heading"), meq("message"))(any(), any())
    }

    "display error template with default messages" in {

      handler.standardErrorTemplate()(fakeRequest)

      verify(errorTemplate).apply(meq("global.error.title"), meq("global.error.heading"), meq("global.error.message"))(any(), any())
    }
  }

  "resolve error" should {
    "handle ReturnToStartException" in {
      val result = Future.successful(handler.resolveError(fakeRequest, ReturnToStartException))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.ChoiceController.displayPage().url)
    }

    "handle ApplicationException" in {
      val result = Future.successful(handler.resolveError(fakeRequest, ApplicationException(Results.BadRequest("bad request"), "message")))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "bad request"
    }

    "handle unexpected exception" in {
      val result = Future.successful(handler.resolveError(fakeRequest, new RuntimeException("some error")))

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(errorTemplate).apply(
        meq("global.error.InternalServerError500.title"),
        meq("global.error.InternalServerError500.heading"),
        meq("global.error.InternalServerError500.message")
      )(any(), any())
    }
  }
}
