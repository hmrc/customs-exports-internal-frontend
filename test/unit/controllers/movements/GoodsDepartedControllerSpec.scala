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
import forms.GoodsDeparted
import forms.GoodsDeparted.DepartureLocation.BackIntoTheUk
import models.cache.{Answers, ArrivalAnswers, Cache, DepartureAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.goods_departed

import scala.concurrent.ExecutionContext.global

class GoodsDepartedControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = mock[goods_departed]

  private def controller(answers: Answers = DepartureAnswers()) =
    new GoodsDepartedController(SuccessfulAuth(), ValidJourney(answers), cacheRepository, stubMessagesControllerComponents(), page)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private def formPassedToPage: Form[GoodsDeparted] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[GoodsDeparted]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private def cachePassedToRepository: Cache = {
    val captor = ArgumentCaptor.forClass(classOf[Cache])
    verify(cacheRepository).upsert(captor.capture())
    captor.getValue
  }

  "Goods Departed Controller on displayPage" should {

    "return 200 (OK)" when {

      "there is no data in cache" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest)

        status(result) mustBe OK
        formPassedToPage.value mustBe empty
      }

      "there is data in cache" in {

        val cachedData = Some(GoodsDeparted(BackIntoTheUk))

        val result = controller(DepartureAnswers(goodsDeparted = cachedData)).displayPage()(getRequest)

        status(result) mustBe OK
        formPassedToPage.value mustBe cachedData
      }
    }

    "return 403 (FORBIDDEN)" when {
      "user is on arrival journey" in {

        givenTheCacheIsEmpty()

        val result = controller(ArrivalAnswers()).displayPage()(getRequest)

        status(result) mustBe FORBIDDEN
      }
    }
  }

  "Goods Departed Controller on saveGoodsDeparted" when {

    "provided with correct form" should {

      "call MovementRepository" in {

        givenTheCacheIsEmpty()
        val correctForm = Json.toJson(GoodsDeparted(BackIntoTheUk))

        await(controller().saveGoodsDeparted()(postRequest(correctForm)))

        cachePassedToRepository.answers mustBe DepartureAnswers(goodsDeparted = Some(GoodsDeparted(BackIntoTheUk)))
      }

      "return 303 (SEE_OTHER) and redirect to Transport page" in {

        givenTheCacheIsEmpty()
        val correctForm = Json.toJson(GoodsDeparted(BackIntoTheUk))

        val result = controller().saveGoodsDeparted()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.TransportController.displayPage().url)
      }
    }

    "provided with incorrect form" should {

      "return 400 (BAD_REQUEST)" in {

        givenTheCacheIsEmpty()

        val incorrectForm = Json.obj("departureLocation" -> "INVALID")

        val result = controller().saveGoodsDeparted()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "user is on arrival journey" should {

      "return 403 (FORBIDDEN)" in {

        givenTheCacheIsEmpty()
        val correctForm = Json.toJson(GoodsDeparted(BackIntoTheUk))

        val result = controller(ArrivalAnswers()).displayPage()(postRequest(correctForm))

        status(result) mustBe FORBIDDEN
      }
    }
  }

}
