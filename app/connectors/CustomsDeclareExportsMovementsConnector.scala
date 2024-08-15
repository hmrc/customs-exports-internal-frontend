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

package connectors

import config.AppConfig
import connectors.exception.MovementsConnectorException
import connectors.exchanges.{ConsolidationExchange, IleQueryExchange, MovementExchange}
import connectors.formats.Implicit.optionFormat
import models.notifications.NotificationFrontendModel
import models.submissions.Submission
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit ec: ExecutionContext)
    extends Connector with Logging {

  protected val httpClient: HttpClientV2 = httpClientV2

  def submit(movementExchange: MovementExchange)(implicit hc: HeaderCarrier): Future[String] =
    post[MovementExchange, HttpResponse](s"${appConfig.customsDeclareExportsMovementsUrl}/movements", movementExchange).andThen {
      case Success(response)  => logSuccessfulExchange("Submit Movement", response.body)
      case Failure(exception) => logFailedExchange("Submit Movement", exception)
    }
      .map(response => handleResponse(response, response.body))

  def submit(consolidationExchange: ConsolidationExchange)(implicit hc: HeaderCarrier): Future[String] =
    post[ConsolidationExchange, HttpResponse](s"${appConfig.customsDeclareExportsMovementsUrl}/consolidation", consolidationExchange).andThen {
      case Success(response)  => logSuccessfulExchange("Submit Consolidation", response.body)
      case Failure(exception) => logFailedExchange("Submit Consolidation", exception)
    }
      .map(response => handleResponse(response, response.body))

  def submit(ileQueryExchange: IleQueryExchange)(implicit hc: HeaderCarrier): Future[String] =
    post[IleQueryExchange, HttpResponse](s"${appConfig.customsDeclareExportsMovementsUrl}/consignment-query", ileQueryExchange).andThen {
      case Success(response)  => logSuccessfulExchange("Submit ILE Query", response.body)
      case Failure(exception) => logFailedExchange("Submit ILE Query", exception)
    }
      .map(res => handleResponse(res, res.body))

  def fetchAllSubmissions(providerId: String)(implicit hc: HeaderCarrier): Future[Seq[Submission]] =
    get[Seq[Submission]](s"${appConfig.customsDeclareExportsMovementsUrl}/submissions", providerIdQueryParam(providerId)).andThen {
      case Success(response)  => logSuccessfulExchange("All Submission fetch", response)
      case Failure(exception) => logFailedExchange("All Submission fetch", exception)
    }

  def fetchSingleSubmission(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[Option[Submission]] =
    get[Option[Submission]](s"${appConfig.customsDeclareExportsMovementsUrl}/submissions/$conversationId", providerIdQueryParam(providerId)).andThen {
      case Success(response)  => logSuccessfulExchange("Single Submission fetch", response)
      case Failure(exception) => logFailedExchange("Single Submission fetch", exception)
    }

  def fetchNotifications(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[Seq[NotificationFrontendModel]] =
    get[Seq[NotificationFrontendModel]](
      s"${appConfig.customsDeclareExportsMovementsUrl}/notifications/$conversationId",
      providerIdQueryParam(providerId)
    ).andThen {
      case Success(response)  => logSuccessfulExchange("All Notifications fetch", response)
      case Failure(exception) => logFailedExchange("All Notifications fetch", exception)
    }

  def fetchQueryNotifications(conversationId: String, providerId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    get[HttpResponse](s"${appConfig.customsDeclareExportsMovementsUrl}/consignment-query/$conversationId", providerIdQueryParam(providerId)).andThen {
      case Success(response)  => logSuccessfulExchange("Ile query response fetch", response.body)
      case Failure(exception) => logFailedExchange("Ile query response fetch", exception)
    }

  private def providerIdQueryParam(providerId: String): Seq[(String, String)] = Seq("providerId" -> providerId)

  private def logSuccessfulExchange[T](`type`: String, payload: T)(implicit fmt: Format[T]): Unit =
    logger.debug(`type` + "\n" + Json.toJson(payload))

  private def logFailedExchange(`type`: String, exception: Throwable): Unit =
    logger.error(`type` + " failed", exception)

  private def handleResponse[T](response: HttpResponse, value: T): T =
    response.status match {
      case Status.ACCEPTED => value
      case _               => throw new MovementsConnectorException(s"Failed with response $response")
    }
}
