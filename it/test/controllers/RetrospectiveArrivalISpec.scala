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

package controllers

import base.IntegrationSpec
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import controllers.movements.routes.LocationController
import controllers.summary.routes.{ArriveDepartSummaryController, MovementConfirmationController}
import forms.{ConsignmentReferenceType, ConsignmentReferences, Location}
import models.cache.RetrospectiveArrivalAnswers
import play.api.test.Helpers._

class RetrospectiveArrivalISpec extends IntegrationSpec {

  "Location Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          RetrospectiveArrivalAnswers(consignmentReferences =
            Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = "GB/123-12345"))
          )
        )

        // When
        val response = get(LocationController.displayPage)

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          RetrospectiveArrivalAnswers(consignmentReferences =
            Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = "GB/123-12345"))
          )
        )

        // When
        val response = post(LocationController.saveLocation, "code" -> "GBAUEMAEMAEMA")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(ArriveDepartSummaryController.displayPage.url)
        theAnswersFor("pid") mustBe Some(
          RetrospectiveArrivalAnswers(
            consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = "GB/123-12345")),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )
      }
    }
  }

  "Movement Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          RetrospectiveArrivalAnswers(
            consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = "GB/123-12345")),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response = get(ArriveDepartSummaryController.displayPage)

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          RetrospectiveArrivalAnswers(
            consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = "GB/123-12345")),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )
        givenTheMovementsBackendAcceptsTheMovement()

        // When
        val response = post(ArriveDepartSummaryController.submitMovementRequest)

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MovementConfirmationController.displayPage.url)
        theAnswersFor("pid") mustBe None
        verify(
          postRequestedForMovement()
            .withRequestBody(equalToJson(s"""{
                   |"eori":"GB1234567890",
                   |"providerId":"pid",
                   |"choice":"RetrospectiveArrival",
                   |"consignmentReference":{"reference":"M","referenceValue":"GB/123-12345"},
                   |"location":{"code":"GBAUEMAEMAEMA"}
                   |}""".stripMargin))
        )

        val submissionPayloadRequestBuilder = postRequestedForAudit()
          .withRequestBody(matchingJsonPath("auditType", equalTo("retrospective-arrival")))
          .withRequestBody(matchingJsonPath("detail.pid", equalTo("pid")))
          .withRequestBody(matchingJsonPath("detail.ConsignmentReferences.reference", equalTo("M")))
          .withRequestBody(matchingJsonPath("detail.ConsignmentReferences.referenceValue", equalTo("GB/123-12345")))
          .withRequestBody(matchingJsonPath("detail.Location.code", equalTo("GBAUEMAEMAEMA")))

        val submissionResultRequestBuilder = postRequestedForAudit()
          .withRequestBody(matchingJsonPath("auditType", equalTo("retrospective-arrival")))
          .withRequestBody(matchingJsonPath("detail.pid", equalTo("pid")))
          .withRequestBody(matchingJsonPath("detail.ucr", equalTo("GB/123-12345")))
          .withRequestBody(matchingJsonPath("detail.ucrType", equalTo("M")))
          .withRequestBody(matchingJsonPath("detail.messageCode", equalTo("RET")))
          .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))

        verifyEventually(submissionPayloadRequestBuilder)
        verifyEventually(submissionResultRequestBuilder)
      }
    }
  }
}
