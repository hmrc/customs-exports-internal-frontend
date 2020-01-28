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
import forms.IleQueryForm.form
import javax.inject.{Inject, Singleton}
import models.UcrBlock
import models.cache.{Answers, IleQuery}
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries.IleQueryResponseExchange
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
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

  private val logger = Logger(this.getClass)

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
    ileQueryRepository.findBySessionIdAndUcr(retrieveSessionId, ucr).flatMap {
      case Some(query) =>
        connector.fetchQueryNotifications(query.conversationId, request.providerId).flatMap { response =>
          response.status match {
            case OK =>
              val queryResponse = Json.parse(response.body).as[Seq[IleQueryResponseExchange]]

              queryResponse match {
                case Seq() => Future.successful(loadingPageResult)
                case response +: _ =>
                  ileQueryRepository.removeByConversationId(query.conversationId).map { _ =>
                    displayQueryResults(response)
                  }
              }
            case _ =>
              logger.warn(s"Movements backend returned status: ${response.status}")
              ileQueryRepository.removeByConversationId(query.conversationId).map { _ =>
                InternalServerError(errorHandler.standardErrorTemplate())
              }
          }
        }

      case None => sendIleQuery(ucr)
    }
  }

  private def loadingPageResult()(implicit request: Request[AnyContent]) =
    Ok(loadingScreenPage()).withHeaders("refresh" -> "5")

  private def displayQueryResults(queryResponse: IleQueryResponseExchange)(implicit request: Request[AnyContent]): Result =
    queryResponse.data match {
      case response: SuccessfulResponseExchangeData =>
        val ducrResult = response.queriedDucr.map(ducr => Ok(ileQueryDucrResponsePage(ducr)))
        val mucrResult = response.queriedMucr.map(mucr => Ok(ileQueryMucrResponsePage(mucr)))

        ducrResult.orElse(mucrResult).getOrElse(loadingPageResult)

      case _: UcrNotFoundResponseExchangeData => Ok("UCR not found")
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
