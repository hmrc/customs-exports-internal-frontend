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
import controllers.exchanges.{AuthenticatedRequest, Operator}
import play.api.mvc.Results.{Forbidden, Redirect}
import play.api.mvc._
import play.api.{Configuration, Environment, Logging, Mode}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.unauthorized

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedAction @Inject() (
  appConfig: AppConfig,
  override val config: Configuration,
  override val env: Environment,
  override val authConnector: StrideAuthConnector,
  mcc: MessagesControllerComponents,
  unauthorizedPage: unauthorized
) extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthorisedFunctions with AuthRedirects with Logging {

  override implicit val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

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

trait AuthRedirects {

  def config: Configuration

  def env: Environment

  private lazy val envPrefix =
    if (env.mode.equals(Mode.Test)) "Test"
    else
      config
        .getOptional[String]("run.mode")
        .getOrElse("Dev")

  private val hostDefaults: Map[String, String] = Map(
    "Dev.external-url.bas-gateway-frontend.host" -> "http://localhost:9553",
    "Dev.external-url.citizen-auth-frontend.host" -> "http://localhost:9029",
    "Dev.external-url.identity-verification-frontend.host" -> "http://localhost:9938",
    "Dev.external-url.stride-auth-frontend.host" -> "http://localhost:9041"
  )

  private def host(service: String): String = {
    val key = s"$envPrefix.external-url.$service.host"
    config.getOptional[String](key).orElse(hostDefaults.get(key)).getOrElse("")
  }

  def strideLoginUrl: String = host("stride-auth-frontend") + "/stride/sign-in"

  final lazy val defaultOrigin: String =
    config
      .getOptional[String]("sosOrigin")
      .orElse(config.getOptional[String]("appName"))
      .getOrElse("undefined")

  def origin: String = defaultOrigin

  def toStrideLogin(successUrl: String, failureUrl: Option[String] = None): Result =
    Redirect(
      strideLoginUrl,
      Map("successURL" -> Seq(successUrl), "origin" -> Seq(origin)) ++ failureUrl.map(f => Map("failureURL" -> Seq(f))).getOrElse(Map())
    )
}
