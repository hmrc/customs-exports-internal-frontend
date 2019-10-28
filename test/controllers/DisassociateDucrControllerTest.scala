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

package controllers

import controllers.actions.AuthenticatedAction
import forms.DisassociateDucr
import models.cache.{Answers, DisassociateUcrAnswers}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repository.MockCache
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.disassociate_ducr

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisassociateDucrControllerTest extends ControllerLayerSpec with MockCache {

  private val ucr = "9AB123456"
  private val submissionService = mock[SubmissionService]
  private val page = new disassociate_ducr(main_template)

  private def controller(auth: AuthenticatedAction, existingAnswers: Answers) =
    new DisassociateDucrController(
      auth,
      ValidJourney(existingAnswers),
      stubMessagesControllerComponents(),
      submissionService,
      cache,
      page
    )

  "GET" should {
    implicit val get = FakeRequest("GET", "/").withCSRFToken

    "return 200 when authenticated" when {
      "empty page answers" in {
        val result = controller(SuccessfulAuth(), DisassociateUcrAnswers(ucr = None)).display(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(DisassociateDucr.form)
      }

      "existing page answers" in {
        val result = controller(SuccessfulAuth(), DisassociateUcrAnswers(ucr = Some(ucr))).display(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(DisassociateDucr.form.fill(ucr))
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }

  "POST" should {
    "return 200 when authenticated" in {
      given(submissionService.submit(any[DisassociateUcrAnswers])).willReturn(Future.successful((): Unit))

      val post = FakeRequest("POST", "/").withFormUrlEncodedBody("ducr" -> ucr).withCSRFToken
      val result = controller(SuccessfulAuth(), DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DisassociateDucrController.display().url)
      theSubmission mustBe DisassociateUcrAnswers(ucr = Some(ucr))
    }

    def theSubmission: DisassociateUcrAnswers = {
      val captor = ArgumentCaptor.forClass(classOf[DisassociateUcrAnswers])
      Mockito.verify(submissionService).submit(captor.capture())
      captor.getValue
    }

    "return 400 when invalid" in {
      implicit val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(SuccessfulAuth(), DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.BAD_REQUEST
      contentAsHtml(result) mustBe page(DisassociateDucr.form.bind(Map[String, String]()))
    }

    "return 403 when unauthenticated" in {
      val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
