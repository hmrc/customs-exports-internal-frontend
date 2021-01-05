/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.exception.MovementsConnectorException
import connectors.exchanges.{ConsolidationExchange, IleQueryExchange, MovementExchange}
import connectors.formats.Implicit.optionFormat
import javax.inject.{Inject, Singleton}
import models.notifications.NotificationFrontendModel
import models.submissions.Submission
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  import CustomsDeclareExportsMovementsConnector._

  private val logger = Logger(this.getClass)

  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def submit(request: MovementExchange)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[MovementExchange, HttpResponse](appConfig.customsDeclareExportsMovementsUrl + Movements, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Movement", response.body)
        case Failure(exception) => logFailedExchange("Submit Movement", exception)
      }
      .map(handleResponse(_, (): Unit))

  def submit(request: ConsolidationExchange)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[ConsolidationExchange, HttpResponse](appConfig.customsDeclareExportsMovementsUrl + Consolidations, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Consolidation", response.body)
        case Failure(exception) => logFailedExchange("Submit Consolidation", exception)
      }
      .map(handleResponse(_, (): Unit))

  def submit(request: IleQueryExchange)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .POST[IleQueryExchange, HttpResponse](appConfig.customsDeclareExportsMovementsUrl + IleQuery, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit ILE Query", response.body)
        case Failure(exception) => logFailedExchange("Submit ILE Query", exception)
      }
      .map(res => handleResponse(res, res.body))

  def fetchAllSubmissions(providerId: String)(implicit hc: HeaderCarrier): Future[Seq[Submission]] =
    httpClient
      .GET[Seq[Submission]](s"${appConfig.customsDeclareExportsMovementsUrl}$Submissions", providerIdQueryParam(providerId))
      .andThen {
        case Success(response)  => logSuccessfulExchange("All Submission fetch", response)
        case Failure(exception) => logFailedExchange("All Submission fetch", exception)
      }

  def fetchSingleSubmission(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](s"${appConfig.customsDeclareExportsMovementsUrl}$Submissions/$conversationId", providerIdQueryParam(providerId))
      .andThen {
        case Success(response)  => logSuccessfulExchange("Single Submission fetch", response)
        case Failure(exception) => logFailedExchange("Single Submission fetch", exception)
      }

  def fetchNotifications(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[Seq[NotificationFrontendModel]] =
    httpClient
      .GET[Seq[NotificationFrontendModel]](
        s"${appConfig.customsDeclareExportsMovementsUrl}$Notifications/$conversationId",
        providerIdQueryParam(providerId)
      )
      .andThen {
        case Success(response)  => logSuccessfulExchange("All Notifications fetch", response)
        case Failure(exception) => logFailedExchange("All Notifications fetch", exception)
      }

  def fetchAllNotificationsForUser(providerId: String)(implicit hc: HeaderCarrier): Future[Seq[NotificationFrontendModel]] =
    httpClient
      .GET[Seq[NotificationFrontendModel]](s"${appConfig.customsDeclareExportsMovementsUrl}$Notifications", providerIdQueryParam(providerId))
      .andThen {
        case Success(response)  => logSuccessfulExchange("All Notifications fetch", response)
        case Failure(exception) => logFailedExchange("All Notifications fetch", exception)
      }

  def fetchQueryNotifications(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .GET[HttpResponse](s"${appConfig.customsDeclareExportsMovementsUrl}$IleQuery/$conversationId", providerIdQueryParam(providerId))
      .andThen {
        case Success(response)  => logSuccessfulExchange("Ile query response fetch", response.body)
        case Failure(exception) => logFailedExchange("Ile query response fetch", exception)
      }

  private def providerIdQueryParam(providerId: String): Seq[(String, String)] = Seq("providerId" -> providerId)

  private def logSuccessfulExchange[T](`type`: String, payload: T)(implicit fmt: Format[T]): Unit =
    logger.debug(`type` + "\n" + Json.toJson(payload))

  private def logFailedExchange(`type`: String, exception: Throwable): Unit =
    logger.error(`type` + " failed", exception)

  private def handleResponse[T](response: HttpResponse, value: T) =
    response.status match {
      case Status.ACCEPTED => value
      case _               => throw new MovementsConnectorException(s"Failed with response $response")
    }
}

object CustomsDeclareExportsMovementsConnector {

  val Movements = "/movements"
  val Consolidations = "/consolidation"
  val Submissions = "/submissions"
  val Notifications = "/notifications"
  val IleQuery = "/consignment-query"
}
