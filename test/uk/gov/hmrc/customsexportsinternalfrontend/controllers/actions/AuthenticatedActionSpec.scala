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

package uk.gov.hmrc.customsexportsinternalfrontend.controllers.actions

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import uk.gov.hmrc.auth.core.{AuthorisationException, NoActiveSession}
import uk.gov.hmrc.customsexportsinternalfrontend.config.AppConfig
import uk.gov.hmrc.customsexportsinternalfrontend.connectors.StrideAuthConnector
import uk.gov.hmrc.customsexportsinternalfrontend.controllers.ControllerLayerSpec
import uk.gov.hmrc.customsexportsinternalfrontend.controllers.exchanges.AuthenticatedRequest
import uk.gov.hmrc.customsexportsinternalfrontend.views.html.unauthorized
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class AuthenticatedActionSpec extends ControllerLayerSpec {

  private val unauthorizedPage: unauthorized = aRealInstanceOf[unauthorized]
  private val appConfig = mock[AppConfig]
  private val config = mock[Configuration]
  private val environment = mock[Environment]
  private val strideConnector = mock[StrideAuthConnector]

  private def action =
    new AuthenticatedAction(
      appConfig,
      config,
      environment,
      strideConnector,
      stubMessagesControllerComponents(),
      unauthorizedPage
    )

  "Invoke" should {
    val request = FakeRequest()
    val block = mock[AuthenticatedRequest[_] => Future[Result]]
    val controllerResponse = mock[Result]

    "delegate to controller" when {
      "auth successful" in {
        given(block.apply(any())).willReturn(Future.successful(controllerResponse))
        given(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
          .willReturn(Future.successful(Some(Credentials("pid", "type"))))

        val result = await(action.invokeBlock(request, block))

        result mustBe controllerResponse
      }
    }

    "return unauthorized" when {
      "auth successful without credentials" in {
        given(block.apply(any())).willReturn(Future.successful(controllerResponse))
        given(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
          .willReturn(Future.successful(None))

        val result = action.invokeBlock(request, block)

        status(result) mustBe FORBIDDEN
      }

      "authorization exception" in {
        given(block.apply(any())).willReturn(Future.successful(controllerResponse))
        given(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
          .willReturn(Future.failed(new AuthorisationException("error") {}))

        val result = action.invokeBlock(request, block)

        status(result) mustBe FORBIDDEN
      }
    }

    "redirect to stride" when {
      "no active session exception" when {
        "running as local" in {
          given(appConfig.runningAsDev).willReturn(true)
          given(config.getOptional(any[String])(any())).willReturn(None)
          given(environment.mode).willReturn(Mode.Dev)
          given(block.apply(any())).willReturn(Future.successful(controllerResponse))
          given(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
            .willReturn(Future.failed(new NoActiveSession("error") {}))

          val result = action.invokeBlock(request, block)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(
            "http://localhost:9041/stride/sign-in?successURL=http%3A%2F%2Flocalhost%2F&origin=undefined"
          )
        }

        "running in environment" in {
          given(appConfig.runningAsDev).willReturn(false)
          given(config.getOptional(any[String])(any())).willReturn(None)
          given(environment.mode).willReturn(Mode.Prod)
          given(block.apply(any())).willReturn(Future.successful(controllerResponse))
          given(strideConnector.authorise(any(), any[Retrieval[Option[Credentials]]]())(any(), any()))
            .willReturn(Future.failed(new NoActiveSession("error") {}))

          val result = action.invokeBlock(request, block)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("http://localhost:9041/stride/sign-in?successURL=%2F&origin=undefined")
        }
      }
    }
  }
}
