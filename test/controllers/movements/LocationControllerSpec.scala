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
import controllers.movements.routes.GoodsDepartedController
import controllers.summary.routes.ArriveDepartSummaryController
import forms.{ConsignmentReferenceType, ConsignmentReferences, Location}
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}

import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import views.html.location

import scala.concurrent.ExecutionContext.global

class LocationControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = mock[location]

  private val consignmentReferences = ConsignmentReferences(ConsignmentReferenceType.D, "referenceValue")

  private def controller(answers: Answers) =
    new LocationController(SuccessfulAuth(), ValidJourney(answers), cacheRepository, stubMessagesControllerComponents(), page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(page)
    when(page.apply(any(), anyString(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private def theResponseForm: Form[Location] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Location]])
    verify(page).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  "Location Controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))
        val result = controller(answers).displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data for Arrival" in {
        val cachedForm = Some(Location("GBAUEMAEMAEMA"))

        val result = controller(ArrivalAnswers(location = cachedForm, consignmentReferences = Some(consignmentReferences))).displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe cachedForm
      }

      "GET displayPage is invoked with data for Retrospective Arrival" in {
        val cachedForm = Some(Location("GBAUEMAEMAEMA"))

        val result =
          controller(RetrospectiveArrivalAnswers(location = cachedForm, consignmentReferences = Some(consignmentReferences)))
            .displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe cachedForm
      }

      "GET displayPage is invoked with data for Departure" in {
        val cachedForm = Some(Location("GBAUEMAEMAEMA"))

        val result =
          controller(DepartureAnswers(location = cachedForm, consignmentReferences = Some(consignmentReferences))).displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe cachedForm
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "POST submit is invoked with incorrect form" in {
        givenTheCacheIsEmpty()
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))
        val invalidForm = Json.toJson(Location("Invalid"))

        val result = controller(answers).saveLocation()(postRequest(invalidForm))

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "Location Controller" when {

    "POST submit is invoked with correct form for arrival" should {

      "call cache upsert method" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        await(controller(ArrivalAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm)))

        theCacheUpserted.answers mustBe an[Option[ArrivalAnswers]]
      }

      "return 303 (SEE_OTHER)" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        val result = controller(ArrivalAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ArriveDepartSummaryController.displayPage.url)
      }
    }

    "POST submit is invoked with correct form for retrospective arrival" should {

      "call cache upsert method" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        await(controller(RetrospectiveArrivalAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm)))

        theCacheUpserted.answers mustBe an[Option[RetrospectiveArrivalAnswers]]
      }

      "return 303 (SEE_OTHER)" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        val result =
          controller(RetrospectiveArrivalAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ArriveDepartSummaryController.displayPage.url)
      }
    }

    "POST submit is invoked with correct form for departure" should {

      "call cache upsert method" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        await(controller(DepartureAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm)))

        theCacheUpserted.answers mustBe an[Option[DepartureAnswers]]
      }

      "return 303 (SEE_OTHER)" in {
        givenTheCacheIsEmpty()

        val correctForm = Json.obj("code" -> "GBAUEMAEMAEMA")

        val result = controller(DepartureAnswers(consignmentReferences = Some(consignmentReferences))).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(GoodsDepartedController.displayPage.url)
      }
    }
  }
}
