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

package controllers.movements

import controllers.ControllerLayerSpec
import forms.ArrivalReference
import models.cache.{ArrivalAnswers, Cache}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.arrival_reference

import scala.concurrent.ExecutionContext.global

class ArrivalReferenceControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = mock[arrival_reference]

  private def controller(answers: ArrivalAnswers = ArrivalAnswers()) =
    new ArrivalReferenceController(SuccessfulAuth(), ValidJourney(answers), cache, stubMessagesControllerComponents(), page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private def theResponseForm: Form[ArrivalReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalReference]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue()
  }

  "Arrival Reference Controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data in cache" in {

        val cachedForm = Some(ArrivalReference(Some("123")))
        givenTheCacheContains(Cache("12345", ArrivalAnswers(arrivalReference = cachedForm)))

        val result = controller(ArrivalAnswers(arrivalReference = cachedForm)).displayPage()(getRequest)

        status(result) mustBe OK

        theResponseForm.value mustBe cachedForm
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "POST submit is invoked with incorrect form" in {

        givenTheCacheIsEmpty()

        val invalidForm = Json.toJson(ArrivalReference(Some("Invalid Reference")))

        val result = controller().submit()(postRequest(invalidForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "POST submit is invoked with correct form" in {

        givenTheCacheIsEmpty()

        val correctForm = Json.toJson(ArrivalReference(Some("VALID123")))

        val result = controller().submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.MovementDetailsController.displayPage().url)
      }
    }
  }
}
