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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, matchingJsonPath, verify}
import forms.{DisassociateKind, DisassociateUcr}
import models.cache.DisassociateUcrAnswers
import play.api.test.Helpers._

class DissociateUcrSpec extends IntegrationSpec {

  "Dissociate UCR Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DisassociateUcrAnswers())

        // When
        val response = get(controllers.consolidations.routes.DisassociateUCRController.display())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", DisassociateUcrAnswers())

        // When
        val response = post(controllers.consolidations.routes.DisassociateUCRController.submit(), "kind" -> "mucr", "mucr" -> "GB/321-54321")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.DisassociateUCRSummaryController.display().url)
        theCacheFor("pid") mustBe Some(
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )
      }
    }
  }

  "Dissociate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )

        // When
        val response = get(controllers.consolidations.routes.DisassociateUCRSummaryController.display())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )
        givenMovementsBackendAcceptsTheConsolidation()

        // When
        val response = post(controllers.consolidations.routes.DisassociateUCRSummaryController.submit())

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.DisassociateUCRConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalTo("""{"providerId":"pid","eori":"GB1234567890","ucr":"GB/321-54321","consolidationType":"DISASSOCIATE_DUCR"}"""))
        )
        verify(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("Disassociate")))
            .withRequestBody(matchingJsonPath("detail.providerId", equalTo("pid")))
            .withRequestBody(matchingJsonPath("detail.ucr", equalTo("GB/321-54321")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
