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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, verify}
import forms.MucrOptions.CreateOrAddValues.Create
import forms.{AssociateKind, AssociateUcr, MucrOptions}
import models.cache.AssociateUcrAnswers
import play.api.test.Helpers._

class AssociateUcrSpec extends IntegrationSpec {

  "UCR Options Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", AssociateUcrAnswers())

        val response = get(controllers.consolidations.routes.MucrOptionsController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", AssociateUcrAnswers())

        val response = post(controllers.consolidations.routes.MucrOptionsController.submit(), "createOrAdd" -> "create", "newMucr" -> "GB/123-12345")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRController.displayPage().url)
        theCacheFor("pid") mustBe Some(AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345"))))
      }
    }
  }

  "Associate UCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345"))))

        val response = get(controllers.consolidations.routes.AssociateUCRController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345"))))

        val response = post(controllers.consolidations.routes.AssociateUCRController.submit(), "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRSummaryController.displayPage().url)
        theCacheFor("pid") mustBe Some(
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(AssociateKind.Mucr, "GB/321-54321"))
          )
        )
      }
    }
  }

  "Associate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(AssociateKind.Mucr, "GB/321-54321"))
          )
        )

        val response = get(controllers.consolidations.routes.AssociateUCRSummaryController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = Create, newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(AssociateKind.Mucr, "GB/321-54321"))
          )
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(controllers.consolidations.routes.AssociateUCRSummaryController.submit())

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUCRConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(
              equalTo(
                """{"providerId":"pid","eori":"GB1234567890","mucr":"GB/123-12345","ucr":"GB/321-54321","consolidationType":"ASSOCIATE_DUCR"}"""
              )
            )
        )
      }
    }
  }
}
