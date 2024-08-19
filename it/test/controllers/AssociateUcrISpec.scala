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

import base.IntegrationSpec
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import controllers.consolidations.routes.{AssociateUcrController, ManageMucrController, MucrOptionsController}
import controllers.summary.routes.{AssociateUcrSummaryController, MovementConfirmationController}
import forms.MucrOptions.CreateOrAddValues.Create
import forms.{AssociateUcr, ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.UcrType._
import models.cache.{AssociateUcrAnswers, Cache}
import play.api.test.Helpers._

class AssociateUcrISpec extends IntegrationSpec {

  "Manage Mucr Page" when {
    "GET" should {
      "return 200 when queried mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = get(ManageMucrController.displayPage)

        status(response) mustBe OK
      }

      "throw IllegalStateException when queried ducr" in {
        givenAuthSuccess("pid")
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(UcrBlock(ucr = "ducr", ucrType = Ducr))))

        intercept[IllegalStateException] {
          await(get(ManageMucrController.displayPage))
        }
      }
    }

    "POST" should {
      "continue for associate this mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(ManageMucrController.submit, "choice" -> ManageMucrChoice.AssociateThisToMucr)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MucrOptionsController.displayPage.url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            childUcr = Some(AssociateUcr(UcrBlock(ucr = "mucr", ucrType = Mucr))),
            manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateThisToMucr))
          )
        )
      }

      "continue for associate another mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(ManageMucrController.submit, "choice" -> ManageMucrChoice.AssociateAnotherUcrToThis)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrController.displayPage.url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            parentMucr = Some(MucrOptions(UcrBlock(ucr = "mucr", ucrType = Mucr))),
            manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherUcrToThis))
          )
        )
      }
    }
  }

  "MUCR Options Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = get(MucrOptionsController.displayPage)

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(MucrOptionsController.submit, "createOrAdd" -> "create", "newMucr" -> "GB/123-12345")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrSummaryController.displayPage.url)
        theAnswersFor("pid") mustBe Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345"))))
      }
    }
  }

  "Associate UCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(
          Cache("pid", Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")))), Some(queryUcr))
        )

        val response = get(AssociateUcrController.displayPage)

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(
          Cache("pid", Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")))), Some(queryUcr))
        )

        val response = post(AssociateUcrController.submit, "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrSummaryController.displayPage.url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
            childUcr = Some(AssociateUcr(Mucr, "GB/321-54321"))
          )
        )
      }
    }
  }

  "Associate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(
          Cache(
            "pid",
            Some(
              AssociateUcrAnswers(
                parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
                childUcr = Some(AssociateUcr(Mucr, "GB/321-54321"))
              )
            ),
            Some(queryUcr)
          )
        )

        val response = get(AssociateUcrSummaryController.displayPage)

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock(ucr = "mucr", ucrType = Mucr)
        givenCacheFor(
          Cache(
            "pid",
            Some(
              AssociateUcrAnswers(
                parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
                childUcr = Some(AssociateUcr(Mucr, "GB/321-54321"))
              )
            ),
            Some(queryUcr)
          )
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(AssociateUcrSummaryController.submit)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MovementConfirmationController.displayPage.url)
        theAnswersFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(
              equalToJson(
                """{"providerId":"pid","eori":"GB1234567890","mucr":"GB/123-12345","ucr":"GB/321-54321","consolidationType":"MucrAssociation"}"""
              )
            )
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("associate")))
            .withRequestBody(matchingJsonPath("detail.pid", equalTo("pid")))
            .withRequestBody(matchingJsonPath("detail.mucr", equalTo("GB/123-12345")))
            .withRequestBody(matchingJsonPath("detail.ducr", equalTo("GB/321-54321")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
