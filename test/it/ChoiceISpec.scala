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

import controllers.routes.ChoiceController
import forms._
import models.UcrType.Mucr
import models.cache._
import models.{UcrBlock, UcrType}
import play.api.test.Helpers._

class ChoiceISpec extends IntegrationSpec {

  "Display Page" should {
    "return 403" in {
      givenAuthFailed()

      val response = get(ChoiceController.displayPage)

      status(response) mustBe FORBIDDEN
    }

    "return 303 if there is no cache" in {
      givenAuthSuccess()

      val response = get(ChoiceController.displayPage)

      status(response) mustBe SEE_OTHER
    }

    "return 200 if there is an UCR in the cache" in {
      givenAuthSuccess("pid")
      givenCacheFor(pid = "pid", queryUcr = UcrBlock(ucr = "GB/123-12345", ucrType = Mucr))

      val response = get(ChoiceController.displayPage)

      status(response) mustBe OK
    }
  }

  "Submit" should {
    "return 403" in {
      givenAuthFailed()

      val response = post(ChoiceController.submit, "choice" -> Choice.Departure.value)

      status(response) mustBe FORBIDDEN
    }

    "continue journey" when {

      val queryResult = UcrBlock(ucr = "GB/123-12345", ucrType = Mucr)

      "Arrival" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.Arrival.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(
          ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, queryResult.ucr)))
        )
      }

      "Retrospective Arrival" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.RetrospectiveArrival.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(
          RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, queryResult.ucr)))
        )
      }

      "Departure" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.Departure.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(
          DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, queryResult.ucr)))
        )
      }

      "Associate UCR" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.AssociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(AssociateUcrAnswers(childUcr = Some(AssociateUcr(queryResult))))
      }

      "Disassociate UCR" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.DisassociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(DisassociateUcrAnswers(ucr = Some(DisassociateUcr(UcrType.Mucr, None, Some(queryResult.ucr)))))
      }

      "Shut MUCR" in {
        givenAuthSuccess("pid")
        givenCacheFor(pid = "pid", queryUcr = queryResult)

        val response = post(ChoiceController.submit, "choice" -> Choice.ShutMUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("pid") mustBe Some(ShutMucrAnswers(shutMucr = Some(ShutMucr(queryResult.ucr))))
      }
    }
  }
}
