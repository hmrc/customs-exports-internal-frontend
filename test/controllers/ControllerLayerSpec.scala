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

package controllers

import config.AppConfig
import connectors.StrideAuthConnector
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Request, Result, Results}
import play.api.{Configuration, Environment}
import controllers.actions.AuthenticatedAction
import controllers.exchanges.{AuthenticatedRequest, Operator}
import views.html.unauthorized
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.ViewTemplates

import scala.concurrent.Future

abstract class ControllerLayerSpec extends WordSpec with ViewTemplates with MustMatchers with MockitoSugar with BeforeAndAfterEach {

  case class SuccessfulAuth(operator: Operator = Operator("0"))
      extends AuthenticatedAction(
        mock[AppConfig],
        mock[Configuration],
        mock[Environment],
        mock[StrideAuthConnector],
        stubMessagesControllerComponents(),
        mock[unauthorized]
      ) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(operator, request))
  }

  case object UnsuccessfulAuth
      extends AuthenticatedAction(
        mock[AppConfig],
        mock[Configuration],
        mock[Environment],
        mock[StrideAuthConnector],
        stubMessagesControllerComponents(),
        mock[unauthorized]
      ) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      Future.successful(Results.Forbidden(""))
  }
}
