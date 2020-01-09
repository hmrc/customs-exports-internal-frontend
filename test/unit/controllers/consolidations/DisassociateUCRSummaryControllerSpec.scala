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
import controllers.actions.AuthenticatedAction
import controllers.storage.FlashKeys
import forms.{DisassociateKind, DisassociateUcr}
import models.ReturnToStartException
import models.cache.{Answers, DisassociateUcrAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.verify
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisassociateUCRSummaryControllerSpec extends ControllerLayerSpec {

  private val ucr = "9AB123456"
  private val disassociation = DisassociateUcr(DisassociateKind.Ducr, Some(ucr), None)
  private val answers = DisassociateUcrAnswers(Answers.fakeEORI, Some(DisassociateUcr(DisassociateKind.Ducr, Some(ucr), None)))
  private val submissionService = mock[SubmissionService]
  private val page = new disassociate_ucr_summary(main_template)

  private def controller(auth: AuthenticatedAction, existingAnswers: Answers) =
    new DisassociateUCRSummaryController(auth, ValidJourney(existingAnswers), stubMessagesControllerComponents(), submissionService, page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/").withCSRFToken

    "return 200 when authenticated" when {
      "empty page answers" in {
        intercept[Throwable] {
          await(controller(SuccessfulAuth(), DisassociateUcrAnswers(ucr = None)).display(get))
        } mustBe ReturnToStartException
      }

      "existing page answers" in {
        val result = controller(SuccessfulAuth(), answers).display(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(disassociation)
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }

  "POST" should {
    "return 200 when authenticated" in {
      given(submissionService.submit(anyString(), any[DisassociateUcrAnswers]())(any())).willReturn(Future.successful((): Unit))

      val post = FakeRequest("POST", "/").withCSRFToken
      val result = controller(SuccessfulAuth(), answers).submit(post)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DisassociateUCRConfirmationController.display().url)
      flash(result).get(FlashKeys.UCR) mustBe Some(ucr)
      flash(result).get(FlashKeys.CONSOLIDATION_KIND) mustBe Some(DisassociateKind.Ducr.toString)
      theSubmission mustBe answers
    }

    def theSubmission: DisassociateUcrAnswers = {
      val captor: ArgumentCaptor[DisassociateUcrAnswers] = ArgumentCaptor.forClass(classOf[DisassociateUcrAnswers])
      verify(submissionService).submit(anyString(), captor.capture())(any())
      captor.getValue
    }

    "return 403 when unauthenticated" in {
      val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
