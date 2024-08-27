/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import base.UnitSpec
import connectors.exchanges.{ArrivalExchange, DepartureExchange, MovementDetailsExchange, RetrospectiveArrivalExchange}
import testdata.CommonTestData.providerId
import testdata.MovementsTestData.{validArrivalAnswers, validDepartureAnswers, validRetrospectiveArrivalAnswers}

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MovementBuilderSpec extends UnitSpec {

  private val zoneId: ZoneId = ZoneId.of("Europe/London")
  private val builder = new MovementBuilder(zoneId)

  "Movement Builder" when {

    "provided with ArrivalAnswers" should {
      "return ArrivalExchange" in {
        val answers = validArrivalAnswers
        val movementDetailsFormatted =
          DateTimeFormatter.ISO_INSTANT.format(answers.arrivalDetails.get.goodsArrivalMoment(zoneId))
        val expectedResult = ArrivalExchange(
          eori = answers.eori.get,
          providerId = providerId,
          consignmentReference = answers.consignmentReferences.get,
          location = answers.location.get,
          movementDetails = MovementDetailsExchange(movementDetailsFormatted)
        )

        val result = builder.createMovementExchange(providerId, answers)

        result match {
          case arrivalExchange: ArrivalExchange =>
            arrivalExchange mustBe expectedResult
          case _ =>
            fail(s"Result is not of type [${ArrivalExchange.getClass.getSimpleName}]")
        }
      }
    }

    "provided with RetrospectiveArrivalAnswers" should {
      "return RetrospectiveArrivalExchange" in {
        val answers = validRetrospectiveArrivalAnswers
        val expectedResult = RetrospectiveArrivalExchange(
          eori = answers.eori.get,
          providerId = providerId,
          consignmentReference = answers.consignmentReferences.get,
          location = answers.location.get
        )

        val result = builder.createMovementExchange(providerId, answers)

        result match {
          case retrospectiveArrivalExchange: RetrospectiveArrivalExchange =>
            retrospectiveArrivalExchange mustBe expectedResult
          case _ =>
            fail(s"Result is not of type [${RetrospectiveArrivalExchange.getClass.getSimpleName}]")
        }
      }
    }

    "provided with DepartureAnswers" should {
      "return DepartureExchange" in {
        val answers = validDepartureAnswers
        val movementDetailsFormatted =
          DateTimeFormatter.ISO_INSTANT.format(answers.departureDetails.get.goodsDepartureMoment(zoneId))
        val expectedResult = DepartureExchange(
          eori = answers.eori.get,
          providerId = providerId,
          consignmentReference = answers.consignmentReferences.get,
          location = answers.location.get,
          movementDetails = MovementDetailsExchange(movementDetailsFormatted),
          transport = answers.transport.get
        )

        val result = builder.createMovementExchange(providerId, answers)

        result match {
          case departureExchange: DepartureExchange =>
            departureExchange mustBe expectedResult
          case _ =>
            fail(s"Result is not of type [${DepartureExchange.getClass.getSimpleName}]")
        }
      }
    }
  }
}
