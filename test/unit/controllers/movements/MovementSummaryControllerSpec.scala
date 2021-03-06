/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsString
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.summary.{arrival_summary_page, departure_summary_page, retrospective_arrival_summary_page}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val arrivalSummaryPage = mock[arrival_summary_page]
  private val retrospectiveArrivalSummaryPage = mock[retrospective_arrival_summary_page]
  private val departureSummaryPage = mock[departure_summary_page]

  private val submissionService: SubmissionService = mock[SubmissionService]

  private def controller(answers: Answers) =
    new MovementSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
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
    when(submissionService.submit(any(), any[MovementAnswers])(any()))
      .thenReturn(Future.successful(ConsignmentReferences(ConsignmentReferenceType.D, "value")))
  }

  override protected def afterEach(): Unit = {
    reset(arrivalSummaryPage, retrospectiveArrivalSummaryPage, departureSummaryPage, submissionService)

    super.afterEach()
  }

  "Movement Summary Controller on displayPage" should {

    "return 200 (OK)" when {

      "there are answers for Arrival" in {

        val result = controller(ArrivalAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(arrivalSummaryPage).apply(any[ArrivalAnswers])(any(), any())
      }

      "there are answers for Retrospective Arrival" in {

        val result = controller(RetrospectiveArrivalAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(retrospectiveArrivalSummaryPage).apply(any[RetrospectiveArrivalAnswers])(any(), any())
      }

      "there are answers for Departure" in {

        val result = controller(DepartureAnswers()).displayPage()(getRequest)

        status(result) mustBe OK
        verify(departureSummaryPage).apply(any[DepartureAnswers])(any(), any())
      }
    }

    "return 403 (FORBIDDEN)" when {

      "user is on the wrong journey " in {

        val result = controller(AssociateUcrAnswers()).displayPage()(getRequest)

        status(result) mustBe FORBIDDEN
      }
    }
  }

  "Movement Summary Controller on submitMovementRequest" when {

    "user is on Arrival journey" should {

      "call SubmissionService" in {

        val cachedAnswers = ArrivalAnswers()

        controller(cachedAnswers).submitMovementRequest()(postRequest(JsString(""))).futureValue

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to MovementConfirmationController" in {

        val result = controller(ArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.displayPage().url)
      }

      "return response with Movement Type in flash" in {

        val result = controller(ArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ARRIVE.toString)
      }
    }

    "user is on Retrospective Arrival journey" should {

      "call SubmissionService" in {

        val cachedAnswers = RetrospectiveArrivalAnswers()

        controller(cachedAnswers).submitMovementRequest()(postRequest(JsString(""))).futureValue

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to MovementConfirmationController" in {

        val result = controller(RetrospectiveArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.displayPage().url)
      }

      "return response with Movement Type in flash" in {

        val result = controller(RetrospectiveArrivalAnswers()).submitMovementRequest()(postRequest(JsString("")))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.RETROSPECTIVE_ARRIVE.toString)
      }
    }

    "user is on Departure journey" should {

      "call SubmissionService" in {

        val cachedAnswers = DepartureAnswers()

        controller(cachedAnswers).submitMovementRequest()(postRequest(JsString(""))).futureValue

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to MovementConfirmationController" in {

        val result = controller(DepartureAnswers()).submitMovementRequest()(postRequest(JsString("")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.movements.routes.MovementConfirmationController.displayPage().url)
      }

      "return response with Movement Type in flash" in {

        val result = controller(DepartureAnswers()).submitMovementRequest()(postRequest(JsString("")))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.DEPART.toString)
      }
    }
  }

}
