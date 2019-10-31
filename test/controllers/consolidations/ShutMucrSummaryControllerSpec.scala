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

package controllers.consolidations

import base.MockCache
import controllers.ControllerLayerSpec
import forms.ShutMucr
import models.ReturnToStartException
import models.cache.{Answers, Cache, ShutMucrAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.{shut_mucr_confirmation, shut_mucr_summary}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ShutMucrSummaryControllerSpec extends ControllerLayerSpec with MockCache {

  private val summaryPage = mock[shut_mucr_summary]
  private val confirmationPage = mock[shut_mucr_confirmation]

  private val submissionService = mock[SubmissionService]

  private def controller(answers: Answers = ShutMucrAnswers()) =
    new ShutMucrSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      stubMessagesControllerComponents(),
      submissionService,
      summaryPage,
      confirmationPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(summaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(confirmationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(summaryPage)

    super.afterEach()
  }

  "Shut Mucr Summary controller" should {

    "return 200 (OK)" when {

      "displayPage is invoked with data in cache" in {

        val cachedForm = Some(ShutMucr("123"))
        givenTheCacheContains(Cache("12345", ShutMucrAnswers(shutMucr = cachedForm)))

        val result = controller(ShutMucrAnswers(shutMucr = cachedForm)).displayPage()(getRequest)

        status(result) mustBe OK
        verify(summaryPage).apply(any())(any(), any())
      }

      "submission finished with success" in {

        when(submissionService.submit(any(), any[ShutMucrAnswers])(any()))
          .thenReturn(Future.successful((): Unit))

        val cachedData = ShutMucrAnswers(shutMucr = Some(ShutMucr("mucr")))

        val result = controller(cachedData).submit()(postRequest)

        status(result) mustBe OK
        verify(confirmationPage).apply(any())(any(), any())
      }

    }

    "throw an exception when" when {

      "data missing when displaying page" in {

        val cachedData = ShutMucrAnswers()

        intercept[ReturnToStartException.type] {
          await(controller(cachedData).displayPage()(getRequest))
        }
      }

    }

  }
}
