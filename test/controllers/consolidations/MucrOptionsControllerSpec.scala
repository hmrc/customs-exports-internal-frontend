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

package controllers.consolidations

import base.MockCache
import controllers.ControllerLayerSpec
import forms.MucrOptions
import models.cache.{AssociateUcrAnswers, Cache}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.mucr_options

import scala.concurrent.ExecutionContext.global

class MucrOptionsControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = mock[mucr_options]

  private def controller(answers: AssociateUcrAnswers = AssociateUcrAnswers()) =
    new MucrOptionsController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), cache, page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private def theResponseForm: Form[MucrOptions] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[MucrOptions]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue()
  }

  "Mucr Options controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data in cache" in {

        val cachedForm = Some(MucrOptions("123"))
        givenTheCacheContains(Cache("12345", AssociateUcrAnswers(mucrOptions = cachedForm)))

        val result = controller(AssociateUcrAnswers(mucrOptions = cachedForm)).displayPage()(getRequest)

        status(result) mustBe OK

        theResponseForm.value mustBe cachedForm
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "POST submit is invoked with incorrect form" in {

        givenTheCacheIsEmpty()

        val result = controller().submit()(postRequest(JsString("")))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "POST submit is invoked with correct form" in {

        givenTheCacheIsEmpty()

        val correctForm = Json.toJson(MucrOptions("GB/12SD-123455ASD"))

        val result = controller().submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.consolidations.routes.AssociateUcrController.displayPage().url)
      }
    }
  }
}
