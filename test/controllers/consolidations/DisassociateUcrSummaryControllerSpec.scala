/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.summary.DisassociateUcrSummaryController
import controllers.summary.routes.MovementConfirmationController
import forms.DisassociateUcr
import models.cache.{Answers, DisassociateUcrAnswers, JourneyType}
import models.summary.SessionHelper
import models.{ReturnToStartException, UcrType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import base.Injector
import testdata.CommonTestData.conversationId
import views.html.summary.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisassociateUcrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with Injector {

  private val submissionService = mock[SubmissionService]
  private val summaryPage = mock[disassociate_ucr_summary]

  private def controller(answers: Answers) =
    new DisassociateUcrSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, summaryPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, summaryPage)
    when(summaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(submissionService.submit(any(), any[DisassociateUcrAnswers])(any())).thenReturn(Future.successful(conversationId))
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, summaryPage)

    super.afterEach()
  }

  private def theResponseData: DisassociateUcr = {
    val disassociateUcrCaptor = ArgumentCaptor.forClass(classOf[DisassociateUcr])
    verify(summaryPage).apply(disassociateUcrCaptor.capture())(any(), any())
    disassociateUcrCaptor.getValue
  }

  private val ucr: DisassociateUcr = DisassociateUcr(UcrType.Ducr, ducr = Some("DUCR"), mucr = None)

  "Disassociate Ucr Summary Controller on displayPage" should {

    "return 200 (OK)" when {
      "display page is invoked with data in cache" in {
        val result = controller(DisassociateUcrAnswers(ucr = Some(ucr))).displayPage(getRequest)

        status(result) mustBe OK
        verify(summaryPage).apply(any())(any(), any())

        theResponseData.ducr.get mustBe "DUCR"
      }
    }

    "throw an ReturnToStartException exception" when {
      "DisassociateUcr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers(None)).displayPage(getRequest))
        } mustBe ReturnToStartException
      }
    }
  }

  "Disassociate Ucr Summary Controller on submit" when {
    "everything works correctly" should {

      "call SubmissionService" in {
        val cachedAnswers = DisassociateUcrAnswers(ucr = Some(ucr))

        controller(cachedAnswers).submit()(postRequest(JsString(""))).futureValue

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to DisassociateUcrConfirmation" in {
        val result = controller(DisassociateUcrAnswers(ucr = Some(ucr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type in session" in {
        val result = controller(DisassociateUcrAnswers(ucr = Some(ucr))).submit()(postRequest(Json.obj()))

        session(result).get(SessionHelper.JOURNEY_TYPE) mustBe Some(JourneyType.DISSOCIATE_UCR.toString)
      }
    }
  }
}
