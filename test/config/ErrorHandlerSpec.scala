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

package config

import base.UnitSpec
import controllers.CSRFSupport
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.error

class ErrorHandlerSpec extends UnitSpec with BeforeAndAfterEach with CSRFSupport {

  private val errorTemplate = mock[error]
  private val errorHandler = new ErrorHandler(stubMessagesApi(), errorTemplate)

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

    "return error template" in {

      errorHandler.standardErrorTemplate("title", "heading", "message")(fakeRequest)

      verify(errorTemplate).apply(meq("title"), meq("heading"), meq("message"))(any(), any())
    }
  }
}
