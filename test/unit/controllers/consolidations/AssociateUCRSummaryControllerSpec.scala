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

import controllers.ControllerLayerSpec
import forms.{AssociateKind, AssociateUcr, MucrOptions}
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.associateucr.associate_ucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AssociateUCRSummaryControllerSpec extends ControllerLayerSpec {

  private val summaryPage = mock[associate_ucr_summary]
  private val submissionService = mock[SubmissionService]

  private def controller(associateUcrAnswers: AssociateUcrAnswers) =
    new AssociateUCRSummaryController(
      SuccessfulAuth(),
      ValidJourney(associateUcrAnswers),
      stubMessagesControllerComponents(),
      submissionService,
      summaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(summaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(summaryPage)

    super.afterEach()
  }

  "GET" should {
    "return 200 (OK)" in {

      val answers = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")), associateUcr = Some(AssociateUcr(AssociateKind.Ducr, "123")))

      val result = controller(answers).displayPage()(getRequest)

      status(result) mustBe OK
      verify(summaryPage).apply(any(), any())(any(), any())
    }

    "throw an exception" when {

      "mucr is missing" in {
        val answers = AssociateUcrAnswers(associateUcr = Some(AssociateUcr(AssociateKind.Ducr, "123")))

        intercept[RuntimeException] {
          await(controller(answers).displayPage()(getRequest))
        } mustBe ReturnToStartException
      }

      "ucr is missing" in {
        val answers = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")))

        intercept[RuntimeException] {
          await(controller(answers).displayPage()(getRequest))
        } mustBe ReturnToStartException
      }

    }
  }

  "POST" should {
    "redirect to confirmation" in {
      when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

      val cachedData = AssociateUcrAnswers(mucrOptions = Some(MucrOptions("123")), associateUcr = Some(AssociateUcr(AssociateKind.Ducr, "123")))

      val result = controller(cachedData).submit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.consolidations.routes.AssociateUCRConfirmationController.display().url)
    }

  }
}
