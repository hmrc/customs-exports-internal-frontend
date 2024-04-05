/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.ControllerLayerSpec
import controllers.summary.ShutMucrSummaryController
import controllers.summary.routes.MovementConfirmationController
import forms.ShutMucr
import models.ReturnToStartException
import models.cache.{Answers, JourneyType, ShutMucrAnswers}
import models.summary.SessionHelper
import org.mockito.ArgumentMatchers.{any, eq => meq}

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsString
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import testdata.CommonTestData.{conversationId, validMucr}
import views.html.summary.shut_mucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ShutMucrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val summaryPage = mock[shut_mucr_summary]
  private val submissionService = mock[SubmissionService]

  private def controller(answers: Answers = ShutMucrAnswers()) =
    new ShutMucrSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, summaryPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(summaryPage, submissionService)
    when(summaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(submissionService.submit(any(), any[ShutMucrAnswers])(any())).thenReturn(Future.successful(conversationId))
  }

  override protected def afterEach(): Unit = {
    reset(summaryPage, submissionService)

    super.afterEach()
  }

  private val shutMucr = ShutMucr(validMucr)

  "Shut Mucr Summary Controller on displayPage" should {

    "return 200 (OK)" when {
      "the cache contains information from shut mucr page" in {
        val result = controller(ShutMucrAnswers(shutMucr = Some(shutMucr))).displayPage(getRequest)

        status(result) mustBe OK
        verify(summaryPage).apply(any())(any(), any())
      }
    }

    "throw ReturnToStartException" when {
      "the cache is empty" in {
        intercept[RuntimeException] {
          await(controller().displayPage(getRequest))
        } mustBe ReturnToStartException
      }
    }
  }

  "Shut Mucr Summary Controller on submit" should {
    "everything works correctly" should {

      "call SubmissionService" in {
        val cachedAnswers = ShutMucrAnswers(shutMucr = Some(shutMucr))

        val result = controller(cachedAnswers).submit()(postRequest(JsString("")))

        await(result)

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return 303 (SEE_OTHER) that redirects to ShutMucrConfirmationController" in {
        val result = controller(ShutMucrAnswers(shutMucr = Some(shutMucr))).submit()(postRequest(JsString("")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type in session" in {
        val result = controller(ShutMucrAnswers(shutMucr = Some(shutMucr))).submit()(postRequest(JsString("")))

        session(result).get(SessionHelper.JOURNEY_TYPE) mustBe Some(JourneyType.SHUT_MUCR.toString)
      }
    }
  }
}
