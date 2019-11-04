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
import connectors.CustomsDeclareExportsMovementsConnector._
import connectors.exchanges.DisassociateDUCRRequest
import forms.ConsignmentReferences
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec {

  private val config = mock[AppConfig]
  given(config.customsDeclareExportsMovementsUrl).willReturn(downstreamURL)

  private val connector = new CustomsDeclareExportsMovementsConnector(config, httpClient)

  "Submit Movement" should {

    "POST to the Back End" in {
      stubFor(
        post(MovementsSubmissionEndpoint)
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request =
        MovementRequest("eori", "provider-id", MovementType.Arrival, ConsignmentReferences("ref", "value"), MovementDetailsRequest("datetime"))
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo(MovementsSubmissionEndpoint))
          .withRequestBody(
            equalTo(
              """{"eori":"eori","providerId":"provider-id","choice":"EAL","consignmentReference":{"reference":"ref","referenceValue":"value"},"movementDetails":{"dateTime":"datetime"}}"""
            )
          )
      )
    }
  }

  "Submit Consolidation" should {

    "POST to the Back End" in {
      stubFor(
        post(ConsolidationsSubmissionEndpoint)
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request = DisassociateDUCRRequest("provider-id", "eori", "ucr")
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo(ConsolidationsSubmissionEndpoint))
          .withRequestBody(equalTo("""{"providerId":"provider-id","eori":"eori","ucr":"ucr","consolidationType":"DISASSOCIATE_DUCR"}"""))
      )
    }
  }

  "fetch all Submissions" should {

    "send GET request to the backend" in {

      val submissionJson = Json.toJson(Seq(exampleSubmissionFrontendModel()))

      stubFor(
        get(s"$FetchAllSubmissionsEndpoint?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(submissionJson.toString))
      )

      connector.fetchAllSubmissions(providerId).futureValue

      val expectedUrl = s"$FetchAllSubmissionsEndpoint?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))
    }
  }

  "fetch single Submission" should {

    "send GET request to the backend" in {

      val submissionJson = Json.toJson(exampleSubmissionFrontendModel())

      stubFor(
        get(s"$FetchSingleSubmissionEndpoint/$conversationId?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(submissionJson.toString))
      )

      connector.fetchSingleSubmission(conversationId, providerId).futureValue

      val expectedUrl = s"$FetchSingleSubmissionEndpoint/$conversationId?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))
    }
  }

  "fetch Notifications" should {

    "send GET request to the backend" in {

      val notificationJson = Json.toJson(Seq(exampleNotificationFrontendModel()))

      stubFor(
        get(s"$FetchNotifications/$conversationId?providerId=$providerId")
          .willReturn(aResponse().withStatus(OK).withBody(notificationJson.toString))
      )

      connector.fetchNotifications(conversationId, providerId).futureValue

      val expectedUrl = s"$FetchNotifications/$conversationId?providerId=$providerId"
      verify(getRequestedFor(urlEqualTo(expectedUrl)))
    }
  }
}
