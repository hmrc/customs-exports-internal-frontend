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

import base.Injector
import controllers.ControllerLayerSpec
import controllers.actions.AuthenticatedAction
import controllers.storage.FlashKeys
import forms.{ConsignmentReferenceType, ConsignmentReferences}
import models.ReturnToStartException
import models.cache.JourneyType
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.movement_confirmation_page

import scala.concurrent.ExecutionContext.Implicits.global

class MovementConfirmationControllerSpec extends ControllerLayerSpec with Injector {

  private val page = instanceOf[movement_confirmation_page]

  private def controller(auth: AuthenticatedAction) =
    new MovementConfirmationController(auth, stubMessagesControllerComponents(), page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      val result = controller(SuccessfulAuth())
        .display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString, FlashKeys.UCR_KIND -> "D", FlashKeys.UCR -> "123"))

      status(result) mustBe Status.OK
      contentAsHtml(result) mustBe page(JourneyType.ARRIVE, ConsignmentReferences(ConsignmentReferenceType.D, "123"))
    }

    "return to start" when {
      "journey type is missing" in {
        intercept[RuntimeException] {
          await(controller(SuccessfulAuth()).display(get.withFlash(FlashKeys.UCR_KIND -> "kind", FlashKeys.UCR -> "123")))
        } mustBe ReturnToStartException
      }

      "ucr kind is missing" in {
        intercept[RuntimeException] {
          await(controller(SuccessfulAuth()).display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString, FlashKeys.UCR -> "123")))
        } mustBe ReturnToStartException
      }

      "ucr is missing" in {
        intercept[RuntimeException] {
          await(
            controller(SuccessfulAuth()).display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString, FlashKeys.UCR_KIND -> "D"))
          )
        } mustBe ReturnToStartException
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
