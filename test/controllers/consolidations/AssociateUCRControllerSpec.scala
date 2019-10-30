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
import forms.{AssociateKind, AssociateUcr, MucrOptions}
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.associate_ucr

import scala.concurrent.ExecutionContext.global

class AssociateUCRControllerSpec extends ControllerLayerSpec with MockCache {

  val associateUcrPage = mock[associate_ucr]

  def controller(associateUcrAnswers: AssociateUcrAnswers) =
    new AssociateUCRController(SuccessfulAuth(), ValidJourney(associateUcrAnswers), stubMessagesControllerComponents(), cache, associateUcrPage)(
      global
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(associateUcrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(associateUcrPage)

    super.afterEach()
  }

  "Associate UCR Controller" should {

    "return 200 (OK)" when {

      "displayPage method is invoked, there is mucr options in cache and associate ucr is empty" in {

        val cachedData = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")))
        val result = controller(cachedData).displayPage()(getRequest)

        status(result) mustBe OK
      }
    }

    "return 303 (SEE_OTHER)" when {

      "correct form is submitted and cache contains mucr options data" in {

        val cachedData = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")))
        val correctForm =
          Json.toJson(AssociateUcr.mapping.unbind(AssociateUcr(AssociateKind.Ducr, "5GB123456789000-123ABC456DEFIIIII")))
        val result = controller(cachedData).submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect and cache contains data from mucr options" in {

        val cachedData = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")))
        val correctForm =
          Json.toJson(AssociateUcr.mapping.unbind(AssociateUcr(AssociateKind.Ducr, "incorrect")))
        val result = controller(cachedData).submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "throw an exception" when {

      "displayPage method is invoked and cache doesn't have mucr opions" in {

        intercept[ReturnToStartException.type] {
          await(controller(AssociateUcrAnswers()).displayPage()(getRequest))
        }
      }

      "submit method is invoked without mucr options" in {

        val correctForm = Json.toJson(AssociateUcr(AssociateKind.Mucr, "5GB123456789000-123ABC456DEFIIIII"))

        intercept[ReturnToStartException.type] {
          await(controller(AssociateUcrAnswers()).submit()(postRequest(correctForm)))
        }
      }
    }
  }
}
