/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.error

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi, errorTemplate: error) extends FrontendErrorHandler {

  private val logger = Logger(this.getClass)

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    errorTemplate(pageTitle, heading, message)

  override def resolveError(request: RequestHeader, exception: Throwable): Result = exception match {
    case ReturnToStartException =>
      logger.warn(s"User Answers was in an invalid state, returning them to the Start Page from [${request.uri}]")
      Results.Redirect(controllers.routes.ChoiceController.displayPage())
    case _ =>
      logger.warn(s"Unexpected Exception was thrown accessing [${request.uri}]", exception)
      super.resolveError(request, exception)
  }

  def standardErrorTemplate()(implicit request: Request[_]): Html =
    errorTemplate("global.error.title", "global.error.heading", "global.error.message")
}
