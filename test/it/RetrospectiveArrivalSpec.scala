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

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import forms.{ConsignmentReferences, Location}
import models.cache.RetrospectiveArrivalAnswers
import play.api.test.Helpers._

class RetrospectiveArrivalSpec extends IntegrationSpec {

  private val date = LocalDate.now()
  private val time = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
  private val datetime = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)

  "Consignment References Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", RetrospectiveArrivalAnswers())

        // When
        val response = get(controllers.movements.routes.ConsignmentReferencesController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", RetrospectiveArrivalAnswers())

        // When
        val response = post(
          controllers.movements.routes.ConsignmentReferencesController.saveConsignmentReferences(),
          "reference" -> "M",
          "mucrValue" -> "GB/123-12345"
        )

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.LocationController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")))
        )
      }
    }
  }

  "Location Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")))
        )

        // When
        val response = get(controllers.movements.routes.LocationController.displayPage())

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
          RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")))
        )

        // When
        val response = post(controllers.movements.routes.LocationController.saveLocation(), "code" -> "GBAUEMAEMAEMA")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.MovementSummaryController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          RetrospectiveArrivalAnswers(
            consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")),
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
            consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response = get(controllers.movements.routes.MovementSummaryController.displayPage())

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
            consignmentReferences = Some(ConsignmentReferences(reference = "M", referenceValue = "GB/123-12345")),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )
        givenTheMovementsBackendAcceptsTheMovement()

        // When
        val response = post(controllers.movements.routes.MovementSummaryController.submitMovementRequest())

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.MovementConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForMovement()
            .withRequestBody(equalToJson(s"""{
                   |"eori":"GB1234567890",
                   |"providerId":"pid",
                   |"choice":"RET",
                   |"consignmentReference":{"reference":"M","referenceValue":"GB/123-12345"},
                   |"location":{"code":"GBAUEMAEMAEMA"}
                   |}""".stripMargin))
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("RetrospectiveArrival")))
            .withRequestBody(matchingJsonPath("detail.providerId", equalTo("pid")))
            .withRequestBody(matchingJsonPath("detail.ucr", equalTo("GB/123-12345")))
            .withRequestBody(matchingJsonPath("detail.ucrType", equalTo("M")))
            .withRequestBody(matchingJsonPath("detail.messageCode", equalTo("RET")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
