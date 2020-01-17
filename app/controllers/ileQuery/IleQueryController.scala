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

package controllers.ileQuery

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.actions.AuthenticatedAction
import controllers.exchanges.AuthenticatedRequest
import forms.IleQuery
import javax.inject.{Inject, Singleton}
import models.UcrBlock
import models.cache.Answers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Cookie, DiscardingCookie, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.FieldValidator.validDucr
import views.html.ile_query

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryController @Inject()(
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  connector: CustomsDeclareExportsMovementsConnector,
  ileQueryPage: ile_query
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayQueryForm(): Action[AnyContent] = authenticate { implicit request =>
    Ok(ileQueryPage(IleQuery.form))
  }

  def submitQueryForm(): Action[AnyContent] = authenticate { implicit request =>
    IleQuery.form.fold(
      formWithErrors => BadRequest(ileQueryPage(formWithErrors)),
      validUcr => Redirect(controllers.ileQuery.routes.IleQueryController.submitQuery(validUcr))
    )
  }

  def submitQuery(ucr: String): Action[AnyContent] = authenticate.async { implicit request =>
    request.cookies.get("conversationId") match {
      case Some(cookie) =>
        connector.fetchQueryNotification(cookie.value, request.providerId).map { notifications =>
          if(notifications.isEmpty) Ok("loading").withHeaders("refresh" -> "1")
          else Ok("Result" + notifications).discardingCookies(DiscardingCookie("conversationId"))
        }
      case None => sendIleQuery(ucr)
    }
  }

  private def sendIleQuery(ucr: String)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    IleQuery.form
      .fillAndValidate(ucr)
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(ileQueryPage(formWithErrors))), // TODO Possible worth change to Not Found
        validUcr => {
          val ileQueryRequest = buildIleQuery(request.providerId, validUcr)

          connector.submit(ileQueryRequest).map { conversationId =>
            redirect(ucr, conversationId)
          }
        }
      )

  private def buildIleQuery(providerId: String, ucr: String): IleQueryExchange = {
    val ucrType = if (validDucr(ucr)) "D" else "M"

    val ucrBlock = UcrBlock(ucr, ucrType)

    IleQueryExchange(Answers.fakeEORI.get, providerId, ucrBlock)
  }

  private def redirect(ucr: String, conversationId: String): Result =
    Redirect(controllers.ileQuery.routes.IleQueryController.submitQuery(ucr))
      .withCookies(
        Cookie(
          name = "conversationId",
          value = conversationId,
          maxAge = Some(60),
          secure = true,
          httpOnly = true
        )
      )
}
