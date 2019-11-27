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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.AppConfig
import connectors.exchanges.{ArrivalExchange, DisassociateDUCRExchange, MovementDetailsExchange}
import forms.{ArrivalReference, ConsignmentReferences, Location}
import models.notifications.ResponseType.ControlResponse
import org.mockito.BDDMockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec with MockitoSugar {

  private val config = mock[AppConfig]
  given(config.customsDeclareExportsMovementsUrl).willReturn(downstreamURL)

  private val connector = new CustomsDeclareExportsMovementsConnector(config, httpClient)

  "Submit Movement" should {

    "POST to the Back End" in {
      stubFor(
        post("/movements")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request =
        ArrivalExchange(
          "eori",
          "provider-id",
          ConsignmentReferences("ref", "value"),
          Location("code"),
          MovementDetailsExchange("datetime"),
          ArrivalReference(Some("reference"))
        )
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/movements"))
          .withRequestBody(equalToJson("""{
                 "eori":"eori",
                 "providerId":"provider-id",
                 "consignmentReference":{"reference":"ref","referenceValue":"value"},
                 "movementDetails":{"dateTime":"datetime"},
                 "location":{"code":"code"},
                 "arrivalReference":{"reference":"reference"},
                 "choice":"EAL"
                 }"""))
      )
    }
  }

  "Submit Consolidation" should {

    "POST to the Back End" in {
      stubFor(
        post("/consolidation")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request = DisassociateDUCRExchange("provider-id", "eori", "ucr")
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/consolidation"))
          .withRequestBody(equalTo("""{"providerId":"provider-id","eori":"eori","ucr":"ucr","consolidationType":"DISASSOCIATE_DUCR"}"""))
      )
    }
  }

  "fetch all Submissions" should {

    "send GET request to the backend" in {

      val expectedSubmission = exampleSubmission()
      val submissionsJson =
        s"""[
           |  {
           |    "uuid":"${expectedSubmission.uuid}",
           |    "eori":"$validEori",
           |    "conversationId":"$conversationId",
           |    "ucrBlocks":[
           |      {
           |        "ucr":"$correctUcr",
           |        "ucrType":"D"
           |      }
           |    ],
           |    "actionType":"Arrival",
           |    "requestTimestamp":"${expectedSubmission.requestTimestamp}"
           |  }
           |]""".stripMargin

      stubFor(
        get(s"/submissions?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(submissionsJson))
      )

      val response = connector.fetchAllSubmissions(providerId).futureValue

      val expectedUrl = s"/submissions?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))

      response mustBe Seq(expectedSubmission)
    }
  }

  "fetch single Submission" should {

    "send GET request to the backend" in {

      val expectedSubmission = exampleSubmission()
      val submissionJson =
        s"""
           |  {
           |    "uuid":"${expectedSubmission.uuid}",
           |    "eori":"$validEori",
           |    "conversationId":"$conversationId",
           |    "ucrBlocks":[
           |      {
           |        "ucr":"$correctUcr",
           |        "ucrType":"D"
           |      }
           |    ],
           |    "actionType":"Arrival",
           |    "requestTimestamp":"${expectedSubmission.requestTimestamp}"
           |  }
           |""".stripMargin

      stubFor(
        get(s"/submissions/$conversationId?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(submissionJson))
      )

      val response = connector.fetchSingleSubmission(conversationId, providerId).futureValue

      val expectedUrl = s"/submissions/$conversationId?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))

      response mustBe Some(expectedSubmission)
    }
  }

  "fetch Notifications" should {

    "send GET request to the backend" in {

      val expectedNotification = exampleNotificationFrontendModel()
      val notificationsJson =
        s"""[
          |   {
          |     "timestampReceived":"${expectedNotification.timestampReceived}",
          |     "conversationId":"$conversationId",
          |     "responseType":"${ControlResponse.value}",
          |     "entries":[
          |       {
          |         "ucrBlock":{
          |           "ucr":"$correctUcr",
          |           "ucrType":"D"
          |         },
          |         "goodsItem":[]
          |       }
          |     ],
          |     "errorCodes":[],
          |     "messageCode":""
          |   }
          |]""".stripMargin

      stubFor(
        get(s"/notifications/$conversationId?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(notificationsJson))
      )

      val response = connector.fetchNotifications(conversationId, providerId).futureValue

      val expectedUrl = s"/notifications/$conversationId?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))

      response mustBe Seq(expectedNotification)
    }
  }
}
