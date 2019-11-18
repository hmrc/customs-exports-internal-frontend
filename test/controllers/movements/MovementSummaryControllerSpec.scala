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

package controllers.movements

import base.MockCache
import controllers.ControllerLayerSpec
import controllers.storage.FlashKeys
import forms.ConsignmentReferences
import models.cache._
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.json.JsString
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.movement_confirmation_page
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementSummaryControllerSpec extends ControllerLayerSpec with MockCache {

  private val mockArrivalSummaryPage = mock[arrival_summary_page]
  private val mockDepartureSummaryPage = mock[departure_summary_page]
  private val mockMovementConfirmationPage = mock[movement_confirmation_page]

  val submissionService: SubmissionService = mock[SubmissionService]

  private def controller(answers: Answers) =
    new MovementSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cache,
      submissionService,
      stubMessagesControllerComponents(),
      mockArrivalSummaryPage,
      mockDepartureSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockArrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalSummaryPage, mockDepartureSummaryPage, mockMovementConfirmationPage)

    super.afterEach()
  }

  "GET" should {
    "return 200 (OK)" when {
      "no answers" in {

        givenTheCacheIsEmpty()

        val result = controller(DepartureAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(mockDepartureSummaryPage).apply(any())(any(), any())
      }

      "some answers" in {

        givenTheCacheContains(Cache("12345", ArrivalAnswers()))

        val result = controller(ArrivalAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(mockArrivalSummaryPage).apply(any())(any(), any())
      }

    }

    "return 403 (FORBIDDEN)" when {
      "user is on the wrong journey " in {
        givenTheCacheIsEmpty()

        val result = controller(AssociateUcrAnswers()).displayPage()(getRequest)

        status(result) mustBe FORBIDDEN
      }
    }

    "POST" should {
      "redirect to confirmation" in {
        givenTheCacheIsEmpty()
        given(submissionService.submit(anyString(), any[MovementAnswers])(any()))
          .willReturn(Future.successful(ConsignmentReferences("D", "9GB23456543")))

        val result = controller(DepartureAnswers()).submitMovementRequest()(postRequest(JsString("")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.display().url)
        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.DEPART.toString)
        flash(result).get(FlashKeys.UCR_KIND) mustBe Some("D")
        flash(result).get(FlashKeys.UCR) mustBe Some("9GB23456543")
      }
    }
  }
}
