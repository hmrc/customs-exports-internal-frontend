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

package handlers

import base.{Injector, UnitSpec}
import controllers.CSRFSupport
import controllers.routes.ChoiceController
import models.ReturnToStartException
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException
import views.html.error_template

import scala.concurrent.ExecutionContext.global

class ErrorHandlerSpec extends UnitSpec with BeforeAndAfterEach with CSRFSupport with Injector {

  private val fakeRequest = FakeRequest("GET", "/foo")

  private val errorTemplate = instanceOf[error_template]
  private val errorHandler = new ErrorHandler(stubMessagesApi(), errorTemplate)(global)

  "ErrorHandler.resolveError" should {

    "handle a ReturnToStartException" in {
      val result = errorHandler.resolveError(fakeRequest, ReturnToStartException)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(ChoiceController.displayPage.url)
    }

    "handle a ApplicationException" in {
      val result = errorHandler.resolveError(fakeRequest, ApplicationException(Results.BadRequest("Bad Request"), "message"))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Bad Request"
    }

    "handle unexpected exceptions" in {
      val result = errorHandler.resolveError(fakeRequest, new RuntimeException("Some Error"))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
