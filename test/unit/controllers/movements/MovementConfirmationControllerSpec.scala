/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.AuthenticatedAction
import controllers.storage.FlashExtractor
import forms.ConsignmentReferences
import models.ReturnToStartException
import models.cache.JourneyType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.confirmation_page

class MovementConfirmationControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val flashExtractor = mock[FlashExtractor]
  private val confirmationPage = mock[confirmation_page]
  private val dummyUcr = "dummyUCR"
  private val dummyUcrType = "dummyUcrType"

  private def controller(auth: AuthenticatedAction = SuccessfulAuth()) =
    new MovementConfirmationController(auth, stubMessagesControllerComponents(), flashExtractor, confirmationPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(flashExtractor, confirmationPage)
    when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(None)
    when(confirmationPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(flashExtractor, confirmationPage)

    super.afterEach()
  }

  "Movement Confirmation Controller on displayPage" should {
    val getRequest = FakeRequest("GET", "/")

    "return 200 when authenticated" when {

      "journey type is ARRIVE" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.ARRIVE))
        when(flashExtractor.extractConsignmentRefs(any[Request[_]])).thenReturn(None)
        val result = controller().displayPage(getRequest)

        status(result) mustBe Status.OK
        verify(confirmationPage).apply(meq(JourneyType.ARRIVE), meq(None), any())(any(), any())
      }

      "journey type is RETROSPECTIVE_ARRIVE" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.RETROSPECTIVE_ARRIVE))
        when(flashExtractor.extractConsignmentRefs(any[Request[_]])).thenReturn(None)
        val result = controller().displayPage()(getRequest)

        status(result) mustBe Status.OK
        verify(confirmationPage).apply(meq(JourneyType.RETROSPECTIVE_ARRIVE), meq(None), any())(any(), any())
      }

      "journey type is DEPART" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.DEPART))
        when(flashExtractor.extractConsignmentRefs(any[Request[_]])).thenReturn(None)
        val result = controller().displayPage(getRequest)

        status(result) mustBe Status.OK
        verify(confirmationPage).apply(meq(JourneyType.DEPART), meq(None), any())(any(), any())
      }
    }

    "call FlashValuesExtractor" when {

      for (journeyType <- List(JourneyType.ARRIVE, JourneyType.DEPART, JourneyType.RETROSPECTIVE_ARRIVE))
        s"journey type is ${journeyType.toString}" in {
          when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(journeyType))
          val request = getRequest.withFlash(
            FlashExtractor.MOVEMENT_TYPE -> journeyType.toString,
            FlashExtractor.UCR -> dummyUcr,
            FlashExtractor.UCR_TYPE -> dummyUcrType
          )

          controller().displayPage(request).futureValue

          val requestCaptor: ArgumentCaptor[Request[_]] = ArgumentCaptor.forClass(classOf[Request[_]])
          verify(flashExtractor).extractMovementType(requestCaptor.capture())
          verify(flashExtractor).extractConsignmentRefs(requestCaptor.capture())
          requestCaptor.getValue.flash.get(FlashExtractor.MOVEMENT_TYPE) mustBe Some(journeyType.toString)
          requestCaptor.getValue.flash.get(FlashExtractor.UCR) mustBe Some(dummyUcr)
          requestCaptor.getValue.flash.get(FlashExtractor.UCR_TYPE) mustBe Some(dummyUcrType)
        }
    }

    "throw ReturnToStartException" when {

      "journey type is missing" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(None)

        intercept[RuntimeException] {
          await(controller().displayPage()(getRequest))
        } mustBe ReturnToStartException
      }

      "journey type is ASSOCIATE_UCR" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.ASSOCIATE_UCR))
        val request = getRequest.withFlash(FlashExtractor.MOVEMENT_TYPE -> JourneyType.ASSOCIATE_UCR.toString)

        intercept[RuntimeException] {
          await(controller().displayPage()(request))
        } mustBe ReturnToStartException
      }

      "journey type is DISSOCIATE_UCR" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.DISSOCIATE_UCR))
        val request = getRequest.withFlash(FlashExtractor.MOVEMENT_TYPE -> JourneyType.DISSOCIATE_UCR.toString)

        intercept[RuntimeException] {
          await(controller().displayPage()(request))
        } mustBe ReturnToStartException
      }

      "journey type is SHUT_MUCR" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.SHUT_MUCR))
        val request = getRequest.withFlash(FlashExtractor.MOVEMENT_TYPE -> JourneyType.SHUT_MUCR.toString)

        intercept[RuntimeException] {
          await(controller().displayPage()(request))
        } mustBe ReturnToStartException
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).displayPage(getRequest)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
