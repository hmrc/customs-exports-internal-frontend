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

package connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import models.requests.MovementRequest
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  private val CustomsDeclareExportsMovementsUrl = s"${appConfig.customsDeclareExportsMovements}"

  private val movementSubmissionUrl: PartialFunction[JourneyType, String] = {
    case JourneyType.ARRIVE | JourneyType.DEPART => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementsSubmissionUri}"
  }

  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def sendArrivalDeclaration(movementRequest: MovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    postRequest(JourneyType.ARRIVE, movementRequest)

  def sendDepartureDeclaration(movementRequest: MovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    postRequest(JourneyType.DEPART, movementRequest)

  private def postRequest(
    actionType: JourneyType,
    movementRequest: MovementRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient
      .POST[MovementRequest, HttpResponse](movementSubmissionUrl(actionType), movementRequest, JsonHeaders)
      .andThen {
        case Success(response) =>
          logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response on ${actionType}. $response")
        case Failure(exception) =>
          logger.warn(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS failure on ${actionType}. $exception ")
      }
}
