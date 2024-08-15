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

package handlers

import controllers.routes.ChoiceController
import models.ReturnToStartException
import play.api.Logging
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.MessagesApi
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.{ApplicationException, FrontendErrorHandler}
import views.html.error_template

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi, errorTemplate: error_template)(implicit executionContext: ExecutionContext)
    extends FrontendErrorHandler with Logging {

  implicit val ec: ExecutionContext = executionContext

  override def standardErrorTemplate(titleKey: String, headingKey: String, messageKey: String)(
    implicit requestHeader: RequestHeader
  ): Future[Html] = {
    implicit val request: Request[_] = Request(requestHeader, "")
    Future.successful(defaultErrorTemplate(titleKey, headingKey, messageKey))
  }

  override def resolveError(rh: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case ReturnToStartException =>
        logger.warn(s"User Answers was in an invalid state, returning them to the Start Page from [${rh.uri}]")
        Future.successful(Results.Redirect(ChoiceController.displayPage))

      case ApplicationException(result, _) => Future.successful(result)

      case _ =>
        logger.warn(s"Unexpected Exception was thron accessing [${rh.uri}]", exception)
        Future.successful(internalServerError(Request(rh, "")))
    }

  def defaultErrorTemplate(
    titleKey: String = "global.error.title",
    headingKey: String = "global.error.heading",
    messageKey: String = "global.error.message"
  )(implicit request: Request[_]): Html = errorTemplate(titleKey, headingKey, messageKey)

  def internalServerError(implicit request: Request[_]): Result =
    InternalServerError(defaultErrorTemplate()).withHeaders(CACHE_CONTROL -> "no-cache")
}
