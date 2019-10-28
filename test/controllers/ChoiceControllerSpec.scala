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
import forms.Choice
import models.cache._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repository.MockCache
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.choice_page

import scala.concurrent.ExecutionContext.Implicits.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val page = new choice_page(main_template)

  private def controller(auth: AuthenticatedAction) =
    new ChoiceController(auth, stubMessagesControllerComponents(), cache, page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/").withCSRFToken

    "return 200 when authenticated" when {
      "empty answers" in {
        givenTheCacheIsEmpty()

        val result = controller(SuccessfulAuth()).displayPage(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(Choice.form())
      }

      "existing answers" in {
        givenTheCacheContains(Cache("pid", ArrivalAnswers()))

        val result = controller(SuccessfulAuth()).displayPage(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(Choice.form().fill(Choice.Arrival))
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).displayPage(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }

  "POST" should {

    "return 200 when authenticated" when {
      "arrival" in {
        val post = FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> Choice.Arrival.value).withCSRFToken
        val result = controller(SuccessfulAuth()).submit(post)

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ArrivalAnswers())
      }

      "departure" in {
        val post = FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> Choice.Departure.value).withCSRFToken
        val result = controller(SuccessfulAuth()).submit(post)

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, DepartureAnswers())
      }

      "associate UCR" in {
        val post = FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> Choice.AssociateUCR.value).withCSRFToken
        val result = controller(SuccessfulAuth()).submit(post)

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, AssociateUcrAnswers())
      }

      "disassociate UCR" in {
        val post = FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> Choice.DisassociateUCR.value).withCSRFToken
        val result = controller(SuccessfulAuth()).submit(post)

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, DissociateUcrAnswers())
      }

      "shut MUCR" in {
        val post = FakeRequest("POST", "/").withFormUrlEncodedBody("choice" -> Choice.ShutMUCR.value).withCSRFToken
        val result = controller(SuccessfulAuth()).submit(post)

        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ChoiceController.displayPage().url)
        theCacheUpserted mustBe Cache(pid, ShutMucrAnswers())
      }
    }

    "return 400 when invalid" in {
      implicit val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(SuccessfulAuth()).submit(post)

      status(result) mustBe Status.BAD_REQUEST
      contentAsHtml(result) mustBe page(Choice.form().bind(Map[String, String]()))
    }

    "return 403 when unauthenticated" in {
      val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(UnsuccessfulAuth).submit(post)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
