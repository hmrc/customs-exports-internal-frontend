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

package controllers

import base.MockCache
import controllers.actions.AuthenticatedAction
import controllers.consolidations.{routes => consolidationRoutes}
import forms.Choice
import forms.Choice._
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.choice_page

import scala.concurrent.ExecutionContext.Implicits.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val choicePage: choice_page = mock[choice_page]

  private def controller(auth: AuthenticatedAction = SuccessfulAuth()) =
    new ChoiceController(auth, stubMessagesControllerComponents(), cache, choicePage)

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(choicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(choicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(choicePage)
    super.afterEach()
  }

  "GET" should {

    "return 200 when authenticated" when {

      "empty answers" in {
        givenTheCacheIsEmpty()

        val result = controller().displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "existing answers" in {
        givenTheCacheContains(Cache("pid", ArrivalAnswers()))

        val result = controller().displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value.get.value mustBe Arrival.value
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).displayPage(getRequest)

      status(result) mustBe FORBIDDEN
    }
  }

  "GET for specific journey" should {

    "return 303 (SEE_OTHER) and redirect to the correct controller" when {

      "user choose arrival" in {

        val result = controller().startSpecificJourney(Choice.Arrival.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
      }

      "user choose departure" in {

        val result = controller().startSpecificJourney(Choice.Departure.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
      }

      "user choose associate ucr" in {

        val result = controller().startSpecificJourney(Choice.AssociateUCR.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
      }

      "user choose dissociate ucr" in {

        val result = controller().startSpecificJourney(Choice.DisassociateUCR.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateDucrController.display().url)
      }

      "user choose shut mucr" in {

        val result = controller().startSpecificJourney(Choice.ShutMUCR.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
      }

      "user choose view submissions" in {

        val result = controller().startSpecificJourney(Choice.ViewSubmissions.value)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
      }
    }

    "throw an exception" when {

      "choice is incorrect" in {

        intercept[IllegalArgumentException] {
          await(controller().startSpecificJourney("Incorrect")(getRequest))
        }
      }
    }
  }

  "POST" should {

    def postWithChoice(choice: Choice): Request[AnyContentAsFormUrlEncoded] =
      FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> choice.value).withCSRFToken

    "return 303 (SEE_OTHER) when authenticated" when {

      "user choose arrival" in {

        val result = controller().submit(postWithChoice(Choice.Arrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ArrivalAnswers(Answers.fakeEORI))
      }

      "user choose departure" in {

        val result = controller().submit(postWithChoice(Choice.Departure))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, DepartureAnswers(Answers.fakeEORI))
      }

      "user choose associate UCR" in {

        val result = controller().submit(postWithChoice(Choice.AssociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, AssociateUcrAnswers())
      }

      "user choose disassociate UCR" in {

        val result = controller().submit(postWithChoice(Choice.DisassociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateDucrController.display().url)
        theCacheUpserted mustBe Cache(pid, DisassociateUcrAnswers())
      }

      "user choose shut MUCR" in {

        val result = controller().submit(postWithChoice(Choice.ShutMUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ShutMucrAnswers())
      }

      "user choose view submissions" in {

        val result = controller(SuccessfulAuth()).submit(postWithChoice(Choice.ViewSubmissions))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ViewSubmissionsAnswers())
      }
    }

    "return 400 when invalid" in {

      val result = controller().submit(FakeRequest("POST", "/").withCSRFToken)

      status(result) mustBe BAD_REQUEST
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).submit(FakeRequest("POST", "/").withCSRFToken)

      status(result) mustBe FORBIDDEN
    }
  }
}
