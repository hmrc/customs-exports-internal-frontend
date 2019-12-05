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

import controllers.ControllerLayerSpec
import controllers.storage.FlashKeys
import forms.{ConsignmentReferenceType, ConsignmentReferences}
import models.cache._
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.json.JsString
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.{MockCache, SubmissionService}
import testdata.CommonTestData.correctUcr
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.movement_confirmation_page
import views.html.summary.{arrival_summary_page, departure_summary_page, retrospective_arrival_summary_page}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementSummaryControllerSpec extends ControllerLayerSpec with MockCache {

  private val arrivalSummaryPage = mock[arrival_summary_page]
  private val retrospectiveArrivalSummaryPage = mock[retrospective_arrival_summary_page]
  private val departureSummaryPage = mock[departure_summary_page]
  private val mockMovementConfirmationPage = mock[movement_confirmation_page]

  val submissionService: SubmissionService = mock[SubmissionService]

  private def controller(answers: Answers) =
    new MovementSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cacheRepository,
      submissionService,
      stubMessagesControllerComponents(),
      arrivalSummaryPage,
      retrospectiveArrivalSummaryPage,
      departureSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(arrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(retrospectiveArrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(departureSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(arrivalSummaryPage, retrospectiveArrivalSummaryPage, departureSummaryPage, mockMovementConfirmationPage)

    super.afterEach()
  }

  "GET" should {
    "return 200 (OK)" when {
      "answers are empty" in {

        val result = controller(ArrivalAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(arrivalSummaryPage).apply(any())(any(), any())
      }

      "there are answers for Arrival" in {

        val result = controller(
          ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr)))
        ).displayPage()(getRequest)

        status(result) mustBe OK
        verify(arrivalSummaryPage).apply(any())(any(), any())
      }

      "there are answers for Retrospective Arrival" in {

        val result =
          controller(
            RetrospectiveArrivalAnswers(
              consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr))
            )
          ).displayPage()(getRequest)

        status(result) mustBe OK
        verify(retrospectiveArrivalSummaryPage).apply(any())(any(), any())
      }

      "there are answers for Departure" in {

        val result = controller(
          DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.D, referenceValue = correctUcr)))
        ).displayPage()(getRequest)

        status(result) mustBe OK
        verify(departureSummaryPage).apply(any())(any(), any())
      }
    }

    "return 403 (FORBIDDEN)" when {
      "user is on the wrong journey " in {

        val result = controller(AssociateUcrAnswers()).displayPage()(getRequest)

        status(result) mustBe FORBIDDEN
      }
    }

    "POST" should {

      "redirect to confirmation" when {

        "user is on Arrival journey" in {

          given(submissionService.submit(anyString(), any[MovementAnswers])(any()))
            .willReturn(Future.successful(ConsignmentReferences(ConsignmentReferenceType.D, "9GB23456543")))

          val result = controller(ArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.display().url)
          flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ARRIVE.toString)
          flash(result).get(FlashKeys.UCR_KIND) mustBe Some("D")
          flash(result).get(FlashKeys.UCR) mustBe Some("9GB23456543")
        }

        "user is on Retrospective Arrival journey" in {

          given(submissionService.submit(anyString(), any[MovementAnswers])(any()))
            .willReturn(Future.successful(ConsignmentReferences(ConsignmentReferenceType.D, "9GB23456543")))

          val result = controller(RetrospectiveArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.display().url)
          flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.RETROSPECTIVE_ARRIVE.toString)
          flash(result).get(FlashKeys.UCR_KIND) mustBe Some("D")
          flash(result).get(FlashKeys.UCR) mustBe Some("9GB23456543")
        }

        "user is on Departure journey" in {

          given(submissionService.submit(anyString(), any[MovementAnswers])(any()))
            .willReturn(Future.successful(ConsignmentReferences(ConsignmentReferenceType.D, "9GB23456543")))

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
}
