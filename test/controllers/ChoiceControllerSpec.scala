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

import controllers.actions.AuthenticatedAction
import controllers.consolidations.{routes => consolidationRoutes}
import forms.Choice
import models.cache._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repository.MockCache
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.choice_page

import scala.concurrent.ExecutionContext.Implicits.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = new choice_page(main_template)

  private def controller(auth: AuthenticatedAction = SuccessfulAuth()) =
    new ChoiceController(auth, stubMessagesControllerComponents(), cache, page)

  private def postWithChoice(choice: Choice): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> choice.value).withCSRFToken

  "GET" should {
    implicit val get = FakeRequest("GET", "/").withCSRFToken

    "return 200 when authenticated" when {
      "empty answers" in {
        givenTheCacheIsEmpty()

        val result = controller().displayPage(get)

        status(result) mustBe OK
        contentAsHtml(result) mustBe page(Choice.form())
      }

      "existing answers" in {
        givenTheCacheContains(Cache("pid", ArrivalAnswers()))

        val result = controller().displayPage(get)

        status(result) mustBe OK
        contentAsHtml(result) mustBe page(Choice.form().fill(Choice.Arrival))
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).displayPage(get)

      status(result) mustBe FORBIDDEN
    }
  }

  "GET for specific journey" should {

    implicit val getRequest = FakeRequest("GET", "/").withCSRFToken

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

    implicit val postRequest = FakeRequest("POST", "/").withCSRFToken

    "return 303 (SEE_OTHER) when authenticated" when {

      "arrival" in {

        val result = controller().submit(postWithChoice(Choice.Arrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ArrivalAnswers(Answers.fakeEORI))
      }

      "departure" in {

        val result = controller().submit(postWithChoice(Choice.Departure))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.ConsignmentReferencesController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, DepartureAnswers(Answers.fakeEORI))
      }

      "associate UCR" in {

        val result = controller().submit(postWithChoice(Choice.AssociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, AssociateUcrAnswers())
      }

      "disassociate UCR" in {

        val result = controller().submit(postWithChoice(Choice.DisassociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateDucrController.display().url)
        theCacheUpserted mustBe Cache(pid, DisassociateUcrAnswers())
      }

      "shut MUCR" in {

        val result = controller().submit(postWithChoice(Choice.ShutMUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ShutMucrAnswers())
      }
    }

    "return 400 when invalid" in {

      val result = controller().submit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsHtml(result) mustBe page(Choice.form().bind(Map[String, String]()))
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).submit(postRequest)

      status(result) mustBe FORBIDDEN
    }
  }
}
