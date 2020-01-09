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

package controllers

import base.UnitSpec
import config.AppConfig
import connectors.StrideAuthConnector
import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import models.cache.Answers
import models.cache.JourneyType.JourneyType
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.libs.json.Writes
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import repositories.CacheRepository
import testdata.CommonTestData.providerId
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.ViewTemplates
import views.html.unauthorized

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ControllerLayerSpec extends UnitSpec with ViewTemplates with BeforeAndAfterEach with CSRFSupport {

  protected val operator = Operator(providerId)

  protected val getRequest: Request[AnyContent] = FakeRequest(GET, "/").withCSRFToken
  protected def postRequest: Request[AnyContent] = FakeRequest(POST, "/").withCSRFToken
  protected def postRequest[T](body: T)(implicit wts: Writes[T]): Request[AnyContentAsJson] =
    FakeRequest("POST", "/").withJsonBody(wts.writes(body)).withCSRFToken

  protected implicit def messages(implicit request: Request[_]): Messages = stubMessagesControllerComponents().messagesApi.preferred(request)
  protected implicit val flashApi: Flash = Flash()

  protected def contentAsHtml(of: Future[Result]): Html = Html(contentAsBytes(of).decodeString(charset(of).getOrElse("utf-8")))

  case class SuccessfulAuth(operator: Operator = operator)
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
      Future.successful(Results.Forbidden)
  }

  case class ValidJourney(answers: Answers) extends JourneyRefiner(mock[CacheRepository]) {
    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
      Future.successful(Right(JourneyRequest(answers, request)))

    override def apply(types: JourneyType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
      if (types.contains(answers.`type`)) ValidJourney(answers) else InValidJourney
  }

  case object InValidJourney extends JourneyRefiner(mock[CacheRepository]) {
    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
      Future.successful(Left(Results.Forbidden))
  }

}
