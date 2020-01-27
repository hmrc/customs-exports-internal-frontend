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

import config.ErrorHandler
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.actions.AuthenticatedAction
import controllers.exchanges.AuthenticatedRequest
import forms.IleQuery.form
import javax.inject.{Inject, Singleton}
import models.UcrBlock
import models.cache.{Answers, IleQuery}
import models.notifications.queries.IleQueryResponse
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.IleQueryRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.FieldValidator.validDucr
import views.html._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryController @Inject()(
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  ileQueryRepository: IleQueryRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  ileQueryPage: ile_query,
  loadingScreenPage: loading_screen,
  ileQueryDucrResponsePage: ile_query_ducr_response,
  ileQueryMucrResponsePage: ile_query_mucr_response
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayQueryForm(): Action[AnyContent] = authenticate { implicit request =>
    Ok(ileQueryPage(form))
  }

  def submitQueryForm(): Action[AnyContent] = authenticate { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(ileQueryPage(formWithErrors)),
        validUcr => Redirect(controllers.ileQuery.routes.IleQueryController.submitQuery(validUcr))
      )
  }

  def submitQuery(ucr: String): Action[AnyContent] = authenticate.async { implicit request =>
    def loadingPageResult = Ok(loadingScreenPage()).withHeaders("refresh" -> "5")

    ileQueryRepository.findBySessionIdAndUcr(retrieveSessionId, ucr).flatMap {
      case Some(query) =>
        connector.fetchQueryNotifications(query.conversationId, request.providerId).flatMap { response =>
          response.status match {
            case OK =>
              ileQueryRepository.removeByConversationId(query.conversationId).map { _ =>
                val queryResponse = Json.parse(response.body).as[IleQueryResponse]

                val ducrResult = queryResponse.queriedDucr.map(ducr => Ok(ileQueryDucrResponsePage(ducr)))
                val mucrResult = queryResponse.queriedMucr.map(mucr => Ok(ileQueryMucrResponsePage(mucr)))

                ducrResult.orElse(mucrResult).getOrElse(loadingPageResult)
              }
            case NO_CONTENT =>
              Future.successful(loadingPageResult)
            case GATEWAY_TIMEOUT =>
              ileQueryRepository.removeByConversationId(query.conversationId).map { _ =>
                InternalServerError(errorHandler.standardErrorTemplate())
              }
            case _ => Future.successful(BadRequest(errorHandler.standardErrorTemplate()))
          }
        }

      case None => sendIleQuery(ucr)
    }
  }

  private def sendIleQuery(ucr: String)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    form
      .fillAndValidate(ucr)
      .fold(
        formWithErrors => Future.successful(BadRequest(ileQueryPage(formWithErrors))),
        validUcr => {
          val ileQueryRequest = buildIleQuery(request.providerId, validUcr)

          connector.submit(ileQueryRequest).flatMap { conversationId =>
            val ileQuery = IleQuery(retrieveSessionId, validUcr, conversationId)

            ileQueryRepository.insert(ileQuery).map { _ =>
              Redirect(controllers.ileQuery.routes.IleQueryController.submitQuery(ucr))
            }
          }
        }
      )

  private def buildIleQuery(providerId: String, ucr: String): IleQueryExchange = {
    val ucrType = if (validDucr(ucr)) "D" else "M"

    val ucrBlock = UcrBlock(ucr, ucrType)

    IleQueryExchange(Answers.fakeEORI.get, providerId, ucrBlock)
  }

  private def retrieveSessionId()(implicit hc: HeaderCarrier): String =
    hc.sessionId.getOrElse(throw new Exception("Session ID is missing")).value
}
