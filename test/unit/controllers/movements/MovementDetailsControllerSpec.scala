/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{LocalDate, LocalTime}

import controllers.ControllerLayerSpec
import forms.common.{Date, Time}
import forms.{ArrivalDetails, DepartureDetails}
import models.cache.{Answers, ArrivalAnswers, Cache, DepartureAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.MovementsTestData
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.{arrival_details, departure_details}

import scala.concurrent.ExecutionContext.global

class MovementDetailsControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = mock[arrival_details]

  private def controller(answers: Answers = ArrivalAnswers()) =
    new MovementDetailsController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cacheRepository,
      stubMessagesControllerComponents(),
      MovementsTestData.movementDetails,
      page,
      mock[departure_details]
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private def theResponseForm: Form[ArrivalDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalDetails]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue()
  }

  "Movement Details Controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data in cache" in {

        val cachedForm = Some(ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now())))
        givenTheCacheContains(Cache("12345", ArrivalAnswers(arrivalDetails = cachedForm)))

        val result = controller(ArrivalAnswers(arrivalDetails = cachedForm)).displayPage()(getRequest)

        status(result) mustBe OK

        theResponseForm.value mustBe cachedForm
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "POST submit is invoked with incorrect form" in {

        givenTheCacheIsEmpty()

        val invalidForm = Json.toJson("")

        val result = controller().saveMovementDetails()(postRequest(invalidForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "POST submit is invoked with correct form for arrival" in {

      givenTheCacheIsEmpty()

      val validArrivalDetails = ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now()))
      val correctForm =
        JsObject(
          Map(
            "dateOfArrival.day" -> JsNumber(validArrivalDetails.dateOfArrival.date.getDayOfMonth),
            "dateOfArrival.month" -> JsNumber(validArrivalDetails.dateOfArrival.date.getMonthValue),
            "dateOfArrival.year" -> JsNumber(validArrivalDetails.dateOfArrival.date.getYear),
            "timeOfArrival.hour" -> JsNumber(validArrivalDetails.timeOfArrival.time.getHour),
            "timeOfArrival.minute" -> JsNumber(validArrivalDetails.timeOfArrival.time.getMinute)
          )
        )

      val result = controller(ArrivalAnswers()).saveMovementDetails()(postRequest(correctForm))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.movements.routes.LocationController.displayPage().url)
    }

    "POST submit is invoked with correct form for departure" in {

      givenTheCacheIsEmpty()

      val validDepartureDetails = DepartureDetails(Date(LocalDate.now()), Time(LocalTime.now()))
      val correctForm =
        JsObject(
          Map(
            "dateOfDeparture.day" -> JsNumber(validDepartureDetails.dateOfDeparture.date.getDayOfMonth),
            "dateOfDeparture.month" -> JsNumber(validDepartureDetails.dateOfDeparture.date.getMonthValue),
            "dateOfDeparture.year" -> JsNumber(validDepartureDetails.dateOfDeparture.date.getYear),
            "timeOfDeparture.hour" -> JsNumber(validDepartureDetails.timeOfDeparture.time.getHour),
            "timeOfDeparture.minute" -> JsNumber(validDepartureDetails.timeOfDeparture.time.getMinute)
          )
        )

      val result = controller(DepartureAnswers()).saveMovementDetails()(postRequest(correctForm))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.movements.routes.LocationController.displayPage().url)
    }

  }
}
