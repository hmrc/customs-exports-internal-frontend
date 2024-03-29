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

package controllers.actions

import scala.concurrent.{ExecutionContext, Future}

import config.AppConfig
import connectors.StrideAuthConnector
import controllers.exchanges.{AuthenticatedRequest, Operator}
import javax.inject.{Inject, Singleton}
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.unauthorized

@Singleton
class AuthenticatedAction @Inject() (
  appConfig: AppConfig,
  override val config: Configuration,
  override val env: Environment,
  override val authConnector: StrideAuthConnector,
  mcc: MessagesControllerComponents,
  unauthorizedPage: unauthorized
) extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthorisedFunctions with AuthRedirects {

  override implicit val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  private val logger = Logger(classOf[AuthenticatedAction])

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    lazy val forbidden = Forbidden(unauthorizedPage()(request, mcc.messagesApi.preferred(request)))

    authorised(AuthProviders(PrivilegedApplication) and Enrolment("write:customs-inventory-linking-exports")).retrieve(Retrievals.credentials) {
      _.map { c =>
        block(AuthenticatedRequest(Operator(c.providerId), request))
      } getOrElse {
        logger.debug("Missing credentials")
        Future.successful(forbidden)
      }
    } recover {
      case _: NoActiveSession =>
        toStrideLogin(if (appConfig.runningAsDev) s"http://${request.host}${request.uri}" else request.uri)
      case e: AuthorisationException =>
        logger.debug("Authentication Failed", e)
        forbidden
    }
  }
}
