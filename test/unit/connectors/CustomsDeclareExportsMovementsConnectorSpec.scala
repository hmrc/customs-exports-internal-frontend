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

package unit.connectors

import config.AppConfig
import connectors.CustomsDeclareExportsMovementsConnector
import forms.Choice
import forms.Choice.{Arrival, Departure}
import models.requests.MovementRequest
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import testdata.MovementsTestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsDeclareExportsMovementsConnectorSpec extends UnitSpec with ScalaFutures {

  import CustomsDeclareExportsMovementsConnectorSpec._

  private trait Test {
    implicit val headerCarrierMock: HeaderCarrier = mock[HeaderCarrier]
    val appConfigMock: AppConfig = mock[AppConfig]
    val httpClientMock: HttpClient = mock[HttpClient]
    val defaultHttpResponse = HttpResponse(OK, Some(Json.toJson("Success")))

    when(httpClientMock.POST[MovementRequest, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(defaultHttpResponse))
    when(httpClientMock.GET(any())(any(), any(), any())).thenReturn(Future.failed(new NotImplementedError()))

    val connector = new CustomsDeclareExportsMovementsConnector(appConfigMock, httpClientMock)
  }

  "CustomsDeclareExportsMovementsConnector on sendArrivalDeclaration" should {

    "call HttpClient, passing URL for Arrival submission endpoint" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementsSubmissionUri}"
      verify(httpClientMock).POST(meq(expectedSubmissionUrl), any(), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      private val request: MovementRequest = movementSubmissionRequest(Arrival)

      connector.sendArrivalDeclaration(request).futureValue

      verify(httpClientMock).POST(any(), meq(request), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      verify(httpClientMock).POST(any(), any(), any())(any(), any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendDepartureDeclaration" should {

    "call HttpClient, passing URL for Departure submission endpoint" in new Test {

      connector.sendDepartureDeclaration(movementSubmissionRequest(Departure)).futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementsSubmissionUri}"
      verify(httpClientMock).POST(meq(expectedSubmissionUrl), any(), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      private val request: MovementRequest = movementSubmissionRequest(Departure)
      connector
        .sendDepartureDeclaration(request)
        .futureValue

      verify(httpClientMock).POST(any(), meq(request), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequest(Departure))
        .futureValue

      verify(httpClientMock).POST(any(), any(), any())(any(), any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector
        .sendDepartureDeclaration(movementSubmissionRequest(Departure))
        .futureValue

      result must equal(defaultHttpResponse)
    }
  }

}

object CustomsDeclareExportsMovementsConnectorSpec {
  def movementSubmissionRequest(movementType: Choice): MovementRequest =
    MovementsTestData.validMovementRequest(movementType)
}
