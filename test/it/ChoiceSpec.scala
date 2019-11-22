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

import forms.Choice
import models.cache._
import play.api.test.Helpers._

class ChoiceSpec extends IntegrationSpec {

  "Display Page" should {
    "return 403" in {
      givenAuthFailed()

      val response = get(controllers.routes.ChoiceController.displayPage())

      status(response) mustBe FORBIDDEN
    }

    "return 200" in {
      givenAuthSuccess()

      val response = get(controllers.routes.ChoiceController.displayPage())

      status(response) mustBe OK
    }
  }

  "Submit" should {
    "return 403" in {
      givenAuthFailed()

      val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.Departure.value)

      status(response) mustBe FORBIDDEN
    }

    "continue journey" when {
      "Departure" in {
        givenAuthSuccess("pid")

        val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.Departure.value)

        status(response) mustBe SEE_OTHER
        theCacheFor("pid") mustBe Some(DepartureAnswers())
      }

      "Arrival" in {
        givenAuthSuccess("pid")

        val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.Arrival.value)

        status(response) mustBe SEE_OTHER
        theCacheFor("pid") mustBe Some(ArrivalAnswers())
      }

      "Associate UCR" in {
        givenAuthSuccess("pid")

        val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.AssociateUCR.value)

        status(response) mustBe SEE_OTHER
        theCacheFor("pid") mustBe Some(AssociateUcrAnswers())
      }

      "Dissociate UCR" in {
        givenAuthSuccess("pid")

        val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.DisassociateUCR.value)

        status(response) mustBe SEE_OTHER
        theCacheFor("pid") mustBe Some(DisassociateUcrAnswers())
      }

      "Shut MUCR" in {
        givenAuthSuccess("pid")

        val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.ShutMUCR.value)

        status(response) mustBe SEE_OTHER
        theCacheFor("pid") mustBe Some(ShutMucrAnswers())
      }
    }
  }
}
