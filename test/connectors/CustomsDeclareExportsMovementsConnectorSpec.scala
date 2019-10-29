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
import connectors.exchanges.DisassociateDUCRRequest
import forms.ConsignmentReferences
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec {

  private val config = mock[AppConfig]
  private val connector = new CustomsDeclareExportsMovementsConnector(config, httpClient)

  "Submit Movement" should {
    given(config.customsDeclareExportsMovements).willReturn(downstreamURL)

    "POST to the Back End" in {
      stubFor(
        post("/movements")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request =
        MovementRequest("eori", Some("provider-id"), MovementType.Arrival, ConsignmentReferences("ref", "value"), MovementDetailsRequest("datetime"))
      await(connector.submit(request))

      verify(
        postRequestedFor(urlEqualTo("/movements"))
          .withRequestBody(
            equalTo(
              "{\"eori\":\"eori\",\"providerId\":\"provider-id\",\"choice\":\"EAL\",\"consignmentReference\":{\"reference\":\"ref\",\"referenceValue\":\"value\"},\"movementDetails\":{\"dateTime\":\"datetime\"}}"
            )
          )
      )
    }
  }

  "Submit Consolidation" should {
    given(config.customsDeclareExportsMovements).willReturn(downstreamURL)

    "POST to the Back End" in {
      stubFor(
        post("/consolidation")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request = DisassociateDUCRRequest("pid", "eori", "ucr")
      await(connector.submit(request))

      verify(
        postRequestedFor(urlEqualTo("/consolidation"))
          .withRequestBody(equalTo("{\"providerId\":\"pid\",\"eori\":\"eori\",\"ucr\":\"ucr\",\"type\":\"ASSOCIATE_DUCR\"}"))
      )
    }
  }

}
