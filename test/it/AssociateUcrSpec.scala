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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import forms.MucrOptions.CreateOrAddValues.Create
import forms.{AssociateUcr, ManageMucrChoice, MucrOptions}
import models.cache.{AssociateUcrAnswers, Cache}
import models.{UcrBlock, UcrType}
import play.api.test.Helpers._

class AssociateUcrSpec extends IntegrationSpec {

  "Manage Mucr Page" when {
    "GET" should {
      "return 200 when queried mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = get(controllers.consolidations.routes.ManageMucrController.displayPage())

        status(response) mustBe OK
      }

      "throw IllegalStateException when queried ducr" in {
        givenAuthSuccess("pid")
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(UcrBlock("ducr", UcrType.Ducr.codeValue))))

        intercept[IllegalStateException] {
          await(get(controllers.consolidations.routes.ManageMucrController.displayPage()))
        }
      }
    }

    "POST" should {
      "continue for associate this mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(controllers.consolidations.routes.ManageMucrController.submit(), "choice" -> ManageMucrChoice.AssociateThisToMucr)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.MucrOptionsController.displayPage().url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            childUcr = Some(AssociateUcr(UcrBlock("mucr", UcrType.Mucr.codeValue))),
            manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateThisToMucr))
          )
        )
      }

      "continue for associate another mucr" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(controllers.consolidations.routes.ManageMucrController.submit(), "choice" -> ManageMucrChoice.AssociateAnotherUcrToThis)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRController.displayPage().url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            parentMucr = Some(MucrOptions(UcrBlock("mucr", UcrType.Mucr.codeValue))),
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
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = get(controllers.consolidations.routes.MucrOptionsController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(Cache("pid", Some(AssociateUcrAnswers()), Some(queryUcr)))

        val response = post(controllers.consolidations.routes.MucrOptionsController.submit(), "createOrAdd" -> "create", "newMucr" -> "GB/123-12345")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRSummaryController.displayPage().url)
        theAnswersFor("pid") mustBe Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345"))))
      }
    }
  }

  "Associate UCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(
          Cache("pid", Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")))), Some(queryUcr))
        )

        val response = get(controllers.consolidations.routes.AssociateUCRController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(
          Cache("pid", Some(AssociateUcrAnswers(parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")))), Some(queryUcr))
        )

        val response = post(controllers.consolidations.routes.AssociateUCRController.submit(), "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRSummaryController.displayPage().url)
        theAnswersFor("pid") mustBe Some(
          AssociateUcrAnswers(
            parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
            childUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )
      }
    }
  }

  "Associate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(
          Cache(
            "pid",
            Some(
              AssociateUcrAnswers(
                parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
                childUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
              )
            ),
            Some(queryUcr)
          )
        )

        val response = get(controllers.consolidations.routes.AssociateUCRSummaryController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        val queryUcr = UcrBlock("mucr", UcrType.Mucr.codeValue)
        givenCacheFor(
          Cache(
            "pid",
            Some(
              AssociateUcrAnswers(
                parentMucr = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
                childUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
              )
            ),
            Some(queryUcr)
          )
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(controllers.consolidations.routes.AssociateUCRSummaryController.submit())

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRConfirmationController.displayPage().url)
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
