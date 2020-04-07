/*
 * Copyright 2020 HM Revenue & Customs
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

import base.Injector
import controllers.ControllerLayerSpec
import controllers.storage.FlashKeys
import forms.DisassociateUcr
import models.{ReturnToStartException, UcrType}
import models.cache.{Answers, DisassociateUcrAnswers, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.disassociateucr.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisassociateUCRSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with Injector {

  private val submissionService = mock[SubmissionService]
  private val summaryPage = mock[disassociate_ucr_summary]

  private def controller(answers: Answers) =
    new DisassociateUCRSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, summaryPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, summaryPage)
    when(summaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(submissionService.submit(any(), any[DisassociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))
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

        val result = controller(DisassociateUcrAnswers(ucr = Some(ucr))).displayPage()(getRequest)

        status(result) mustBe OK
        verify(summaryPage).apply(any())(any(), any())

        theResponseData.ducr.get mustBe "DUCR"
      }
    }

    "throw an ReturnToStartException exception" when {

      "DisassociateUcr is missing during displaying page" in {

        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers(None)).displayPage()(getRequest))
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
        redirectLocation(result) mustBe Some(controllers.consolidations.routes.DisassociateUCRConfirmationController.displayPage().url)

      }

      "return response with Movement Type in flash" in {

        val result = controller(DisassociateUcrAnswers(ucr = Some(ucr))).submit()(postRequest(Json.obj()))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.DISSOCIATE_UCR.toString)

      }
    }
  }

}
