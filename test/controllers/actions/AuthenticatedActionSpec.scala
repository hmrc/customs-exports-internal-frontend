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

package controllers.actions

import config.AppConfig
import connectors.StrideAuthConnector
import controllers.ControllerLayerSpec
import controllers.exchanges.AuthenticatedRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{verify, when}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Configuration, Environment, Mode}
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.providerId
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisationException, Enrolment, NoActiveSession}
import views.html.unauthorized

import scala.concurrent.Future

class AuthenticatedActionSpec extends ControllerLayerSpec {

  private val unauthorizedPage: unauthorized = mock[unauthorized]
  private val appConfig = mock[AppConfig]
  private val config = mock[Configuration]
  private val environment = mock[Environment]
  private val strideConnector = mock[StrideAuthConnector]

  private def action =
    new AuthenticatedAction(appConfig, config, environment, strideConnector, stubMessagesControllerComponents(), unauthorizedPage)

  "Invoke" should {
    val request = FakeRequest()
    val block = mock[AuthenticatedRequest[?] => Future[Result]]
    val controllerResponse = mock[Result]

    "delegate to controller" when {
      "auth successful" in {
        when(block.apply(any())).thenReturn(Future.successful(controllerResponse))
        when(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
          .thenReturn(Future.successful(Some(Credentials(providerId, "type"))))

        val result = await(action.invokeBlock(request, block))

        result mustBe controllerResponse
        theAuthCondition mustBe (AuthProviders(PrivilegedApplication) and Enrolment("write:customs-inventory-linking-exports"))
      }

      def theAuthCondition: Predicate = {
        val captor = ArgumentCaptor.forClass(classOf[Predicate])
        verify(strideConnector).authorise(captor.capture(), any[Retrieval[Option[Credentials]]])(any(), any())
        captor.getValue
      }
    }

    "return unauthorized" when {
      "auth successful without credentials" in {
        when(block.apply(any())).thenReturn(Future.successful(controllerResponse))
        when(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any())).thenReturn(Future.successful(None))
        when(unauthorizedPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)

        val result = action.invokeBlock(request, block)

        status(result) mustBe FORBIDDEN
      }

      "authorization exception" in {
        when(block.apply(any())).thenReturn(Future.successful(controllerResponse))
        when(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
          .thenReturn(Future.failed(new AuthorisationException("error") {}))
        when(unauthorizedPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)

        val result = action.invokeBlock(request, block)

        status(result) mustBe FORBIDDEN
      }
    }

    "redirect to stride" when {
      "no active session exception" when {
        "running as local" in {
          when(appConfig.runningAsDev).thenReturn(true)
          when(config.getOptional(any[String])(any())).thenReturn(None)
          when(environment.mode).thenReturn(Mode.Dev)
          when(block.apply(any())).thenReturn(Future.successful(controllerResponse))
          when(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
            .thenReturn(Future.failed(new NoActiveSession("error") {}))

          val result = action.invokeBlock(request, block)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9041/stride/sign-in?successURL=http%3A%2F%2Flocalhost%2F&origin=undefined")
        }

        "running in environment" in {
          when(appConfig.runningAsDev).thenReturn(false)
          when(config.getOptional(any[String])(any())).thenReturn(None)
          when(environment.mode).thenReturn(Mode.Prod)
          when(block.apply(any())).thenReturn(Future.successful(controllerResponse))
          when(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
            .thenReturn(Future.failed(new NoActiveSession("error") {}))

          val result = action.invokeBlock(request, block)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9041/stride/sign-in?successURL=%2F&origin=undefined")
        }
      }
    }
  }
}
