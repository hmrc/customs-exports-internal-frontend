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

import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, verify}
import forms.GoodsDeparted.DepartureLocation
import forms._
import forms.common.{Date, Time}
import models.cache.DepartureAnswers
import play.api.test.Helpers._

class DepartureSpec extends IntegrationSpec {

  private val date = LocalDate.now()
  private val time = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
  private val datetime = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)

  "Consignment References Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DepartureAnswers())

        // When
        val response = get(controllers.movements.routes.ConsignmentReferencesController.displayPage())

        // TThen
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DepartureAnswers())

        // When
        val response = post(
          controllers.movements.routes.ConsignmentReferencesController.saveConsignmentReferences(),
          "reference" -> "M",
          "mucrValue" -> "GB/123-12345"
        )

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.MovementDetailsController.displayPage().url)
        theCacheFor("pid") mustBe Some(DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))
      }
    }
  }

  "Movement Details Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

        // When
        val response = get(controllers.movements.routes.MovementDetailsController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

        // When
        val response = post(
          controllers.movements.routes.MovementDetailsController.saveMovementDetails(),
          "dateOfDeparture.day" -> date.getDayOfMonth.toString,
          "dateOfDeparture.month" -> date.getMonthValue.toString,
          "dateOfDeparture.year" -> date.getYear.toString,
          "timeOfDeparture.hour" -> time.getHour.toString,
          "timeOfDeparture.minute" -> time.getMinute.toString
        )

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.LocationController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
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
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
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
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
        )

        // When
        val response = post(controllers.movements.routes.LocationController.saveLocation(), "code" -> "GBAUEMAEMAEMA")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.GoodsDepartedController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )
      }
    }
  }

  "Goods Departed Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response = get(controllers.movements.routes.GoodsDepartedController.displayPage())

        // TThen
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response = post(controllers.movements.routes.GoodsDepartedController.saveGoodsDeparted(), "departureLocation" -> "outOfTheUk")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.TransportController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk))
          )
        )
      }
    }
  }

  "Transport Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk))
          )
        )

        // When
        val response = get(controllers.movements.routes.TransportController.displayPage())

        // TThen
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk))
          )
        )

        // When
        val response = post(
          controllers.movements.routes.TransportController.saveTransport(),
          "modeOfTransport" -> "1",
          "nationality" -> "FR",
          "transportId" -> "123"
        )

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.movements.routes.MovementSummaryController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk)),
            transport = Some(Transport(Some("1"), Some("FR"), Some("123")))
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
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk)),
            transport = Some(Transport(Some("1"), Some("FR"), Some("123")))
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
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            goodsDeparted = Some(GoodsDeparted(DepartureLocation.OutOfTheUk)),
            transport = Some(Transport(Some("1"), Some("FR"), Some("123")))
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
                   |"choice":"EDL",
                   |"consignmentReference":{"reference":"M","referenceValue":"GB/123-12345"},
                   |"movementDetails":{"dateTime":"$datetime"},
                   |"location":{"code":"GBAUEMAEMAEMA"},
                   |"transport":{"modeOfTransport":"1","nationality":"FR","transportId":"123"}
                   |}""".stripMargin))
        )
      }
    }
  }
}
