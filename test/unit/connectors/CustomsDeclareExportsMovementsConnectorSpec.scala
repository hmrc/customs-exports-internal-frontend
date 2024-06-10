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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.AppConfig
import connectors.exception.MovementsConnectorException
import connectors.exchanges.{ArrivalExchange, DisassociateDUCRExchange, IleQueryExchange, MovementDetailsExchange}
import forms.{ConsignmentReferenceType, ConsignmentReferences, Location}
import models.UcrBlock
import models.UcrType.Ducr
import models.notifications.queries.DucrInfo
import models.notifications.queries.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import org.mockito.BDDMockito._
import org.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec {

  implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
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
          ConsignmentReferences(ConsignmentReferenceType.D, "value"),
          Location("code"),
          MovementDetailsExchange("datetime")
        )
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/movements"))
          .withRequestBody(equalToJson("""{
                 "eori":"eori",
                 "providerId":"provider-id",
                 "consignmentReference":{"reference":"D","referenceValue":"value"},
                 "movementDetails":{"dateTime":"datetime"},
                 "location":{"code":"code"},
                 "choice":"Arrival"
                 }"""))
      )
    }

    "Handle failure from back end" in {
      stubFor(
        post("/movements")
          .willReturn(
            aResponse()
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )

      val request =
        ArrivalExchange(
          "eori",
          "provider-id",
          ConsignmentReferences(ConsignmentReferenceType.D, "value"),
          Location("code"),
          MovementDetailsExchange("datetime")
        )

      intercept[MovementsConnectorException] {
        await(connector.submit(request))
      }
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
          .withRequestBody(equalToJson("""{"ucr":"ucr","providerId":"provider-id","consolidationType":"DucrDisassociation", "eori":"eori"}"""))
      )
    }

    "Handle failure from back end" in {
      stubFor(
        post("/consolidation")
          .willReturn(
            aResponse()
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )

      val request = DisassociateDUCRExchange("provider-id", "eori", "ucr")

      intercept[MovementsConnectorException] {
        await(connector.submit(request))
      }
    }
  }

  "Submit ILE Query" should {

    "POST to the Back End" in {
      stubFor(
        post("/consignment-query")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(s"$conversationId")
              .withHeader("Content-Type", "application/json")
          )
      )

      val request = IleQueryExchange("eori", "provider-id", UcrBlock(ucr = "ucr", ucrType = Ducr))
      val result = connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/consignment-query"))
          .withRequestBody(equalToJson("""{"eori":"eori","providerId":"provider-id","ucrBlock":{"ucr":"ucr","ucrType":"D"}}""".stripMargin))
      )

      result mustBe conversationId
    }

    "Handle failure from back end" in {
      stubFor(
        post("/consignment-query")
          .willReturn(
            aResponse()
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )

      val request = IleQueryExchange("eori", "provider-id", UcrBlock(ucr = "ucr", ucrType = Ducr))

      intercept[MovementsConnectorException] {
        await(connector.submit(request))
      }
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
           |    "requestTimestamp": ${Json.toJson(expectedSubmission.requestTimestamp)}
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
           |    "requestTimestamp":${Json.toJson(expectedSubmission.requestTimestamp)}
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

  "fetch Query Notifications" when {
    val expectedDucrInfo = DucrInfo(ucr = correctUcr, declarationId = "declarationId")
    val expectedNotification = SuccessfulResponseExchangeData(queriedDucr = Some(expectedDucrInfo))
    val notificationJson =
      s"""
           |  {
           |    "queriedDucr": {
           |      "ucr":"$correctUcr",
           |      "declarationId":"declarationId",
           |      "movements":[],
           |      "goodsItem":[]
           |    },
           |    "childDucrs":[],
           |    "childMucrs":[]
           |  }
           |""".stripMargin

    "everything works correctly" should {

      "send GET request to the backend" in {
        stubFor(
          get(s"/consignment-query/$conversationId?providerId=$providerId")
            .willReturn(aResponse().withStatus(OK).withBody(notificationJson))
        )

        connector.fetchQueryNotifications(conversationId, providerId).futureValue

        val expectedUrl = s"/consignment-query/$conversationId?providerId=$providerId"
        verify(getRequestedFor(urlEqualTo(expectedUrl)))
      }

      "return HttpResponse with Ok (200) status and Notification in body" in {
        stubFor(
          get(s"/consignment-query/$conversationId?providerId=$providerId")
            .willReturn(aResponse().withStatus(OK).withBody(notificationJson))
        )

        val response = connector.fetchQueryNotifications(conversationId, providerId).futureValue

        response.status mustBe OK
        Json.parse(response.body).as[SuccessfulResponseExchangeData] mustBe expectedNotification
      }
    }

    "received FailedDependency (424) response" should {
      "return HttpResponse with FailedDependency status" in {
        stubFor(
          get(s"/consignment-query/$conversationId?providerId=$providerId")
            .willReturn(aResponse().withStatus(FAILED_DEPENDENCY))
        )

        val response = connector.fetchQueryNotifications(conversationId, providerId).futureValue

        response.status mustBe FAILED_DEPENDENCY
      }
    }

    "received InternalServerError (500) response" should {
      "return Internal server error" in {
        stubFor(
          get(s"/consignment-query/$conversationId?providerId=$providerId")
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val response = connector.fetchQueryNotifications(conversationId, providerId).futureValue

        response.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
