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

package controllers.movements

import controllers.ControllerLayerSpec
import controllers.movements.routes.LocationController
import forms.common.{Date, Time}
import forms.{ArrivalDetails, DepartureDetails}
import models.cache.{Answers, ArrivalAnswers, Cache, DepartureAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.MovementsTestData
import views.html.{arrival_details, departure_details}

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.concurrent.ExecutionContext.global

class MovementDetailsControllerSpec extends ControllerLayerSpec with MockCache {

  private val mockArrivalDetailsPage = mock[arrival_details]
  private val mockDepartureDetailsPage = mock[departure_details]

  private def controller(answers: Answers = ArrivalAnswers()) =
    new MovementDetailsController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cacheRepository,
      stubMessagesControllerComponents(),
      MovementsTestData.movementDetails,
      mockArrivalDetailsPage,
      mockDepartureDetailsPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockArrivalDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalDetailsPage, mockDepartureDetailsPage)

    super.afterEach()
  }

  private def arrivalResponseForm: Form[ArrivalDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalDetails]])
    verify(mockArrivalDetailsPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Movement Details Controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {
        whenTheCacheIsEmpty()

        val result = controller().displayPage(getRequest)

        status(result) mustBe OK
        arrivalResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data in cache" in {
        val cachedForm = Some(ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now())))
        whenTheCacheContains(Cache("12345", Some(ArrivalAnswers(arrivalDetails = cachedForm)), None))

        val result = controller(ArrivalAnswers(arrivalDetails = cachedForm)).displayPage(getRequest)

        status(result) mustBe OK

        arrivalResponseForm.value mustBe cachedForm
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "POST submit is invoked with incorrect form" in {
        whenTheCacheIsEmpty()

        val invalidForm = Json.toJson("")

        val result = controller().saveMovementDetails()(postRequest(invalidForm))

        status(result) mustBe BAD_REQUEST
      }

      "date is in the future" in {
        val tomorrow = LocalDateTime.now().plusDays(1)
        val result = controller(ArrivalAnswers()).saveMovementDetails()(
          postRequest(
            "dateOfArrival.day" -> s"${tomorrow.getDayOfMonth}",
            "dateOfArrival.month" -> s"${tomorrow.getMonthValue}",
            "dateOfArrival.year" -> s"${tomorrow.getYear}",
            "timeOfArrival.hour" -> "10",
            "timeOfArrival.minute" -> "35",
            "timeOfArrival.ampm" -> "AM"
          )
        )

        status(result) mustBe BAD_REQUEST

        arrivalResponseForm.errors must be(
          Seq(FormError("dateOfArrival", "arrival.details.error.future"), FormError("timeOfArrival", "arrival.details.error.future"))
        )
      }

      "date is in the past" in {
        val lastYear = LocalDateTime.now().minusYears(1)
        val result = controller(ArrivalAnswers()).saveMovementDetails()(
          postRequest(
            "dateOfArrival.day" -> s"${lastYear.getDayOfMonth}",
            "dateOfArrival.month" -> s"${lastYear.getMonthValue}",
            "dateOfArrival.year" -> s"${lastYear.getYear}",
            "timeOfArrival.hour" -> "10",
            "timeOfArrival.minute" -> "35",
            "timeOfArrival.ampm" -> "AM"
          )
        )

        status(result) mustBe BAD_REQUEST

        arrivalResponseForm.errors must be(
          Seq(FormError("dateOfArrival", "arrival.details.error.overdue"), FormError("timeOfArrival", "arrival.details.error.overdue"))
        )
      }
    }

    "POST submit is invoked with correct form for arrival" in {
      whenTheCacheIsEmpty()

      val validArrivalDetails = ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now()))
      val correctForm = List(
        "dateOfArrival.day" -> validArrivalDetails.dateOfArrival.date.getDayOfMonth.toString,
        "dateOfArrival.month" -> validArrivalDetails.dateOfArrival.date.getMonthValue.toString,
        "dateOfArrival.year" -> validArrivalDetails.dateOfArrival.date.getYear.toString,
        "timeOfArrival.hour" -> validArrivalDetails.timeOfArrival.getClockHour.toString,
        "timeOfArrival.minute" -> validArrivalDetails.timeOfArrival.getMinute.toString,
        "timeOfArrival.ampm" -> validArrivalDetails.timeOfArrival.getAmPm
      )

      val result = controller(ArrivalAnswers()).saveMovementDetails()(postRequest(correctForm: _*))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(LocationController.displayPage.url)
    }

    "POST submit is invoked with correct form for departure" in {
      whenTheCacheIsEmpty()

      val validDepartureDetails = DepartureDetails(Date(LocalDate.now()), Time(LocalTime.now()))
      val correctForm = List(
        "dateOfDeparture.day" -> validDepartureDetails.dateOfDeparture.date.getDayOfMonth.toString,
        "dateOfDeparture.month" -> validDepartureDetails.dateOfDeparture.date.getMonthValue.toString,
        "dateOfDeparture.year" -> validDepartureDetails.dateOfDeparture.date.getYear.toString,
        "timeOfDeparture.hour" -> validDepartureDetails.timeOfDeparture.getClockHour.toString,
        "timeOfDeparture.minute" -> validDepartureDetails.timeOfDeparture.getMinute.toString,
        "timeOfDeparture.ampm" -> validDepartureDetails.timeOfDeparture.getAmPm
      )

      val result = controller(DepartureAnswers()).saveMovementDetails()(postRequest(correctForm: _*))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(LocationController.displayPage.url)
    }
  }
}
