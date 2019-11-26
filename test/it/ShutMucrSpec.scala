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

import forms.ShutMucr
import models.cache.ShutMucrAnswers
import play.api.test.Helpers._
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, containing, verify}

class ShutMucrSpec extends IntegrationSpec {

  "Enter MUCR Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers())

        // When
        val response = get(controllers.consolidations.routes.ShutMucrController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers())

        // When
        val response = post(controllers.consolidations.routes.ShutMucrController.submit(), "mucr" -> "GB/123-12345")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMucrSummaryController.displayPage().url)
        theCacheFor("pid") mustBe Some(ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
      }
    }
  }

  "Shut MUCR Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))

        // When
        val response = get(controllers.consolidations.routes.ShutMucrSummaryController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
        givenMovementsBackendAcceptsTheConsolidation()

        // When
        val response = post(controllers.consolidations.routes.ShutMucrSummaryController.submit())

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMUCRConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalTo("""{"providerId":"pid","eori":"GB1234567890","mucr":"GB/123-12345","consolidationType":"SHUT_MUCR"}"""))
        )
        verify(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", containing("ShutMucr")))
            .withRequestBody(matchingJsonPath("detail.providerId", containing("pid")))
            .withRequestBody(matchingJsonPath("detail.mucr", containing("GB/123-12345")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", containing("Success")))
        )
      }
    }
  }
}
