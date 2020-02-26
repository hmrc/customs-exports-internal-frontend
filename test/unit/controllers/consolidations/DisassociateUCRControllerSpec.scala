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
import controllers.actions.AuthenticatedAction
import forms.{DisassociateKind, DisassociateUcr}
import models.cache.{Answers, DisassociateUcrAnswers}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MockCache
import views.html.disassociateucr.disassociate_ucr

import scala.concurrent.ExecutionContext.Implicits.global

class DisassociateUCRControllerSpec extends ControllerLayerSpec with MockCache with Injector {

  private val ucr = "9AB123456"
  private val disassociation = DisassociateUcr(DisassociateKind.Ducr, Some(ucr), None)
  private val page = instanceOf[disassociate_ucr]

  private def controller(auth: AuthenticatedAction, existingAnswers: Answers) =
    new DisassociateUCRController(auth, ValidJourney(existingAnswers), stubMessagesControllerComponents(), cacheRepository, page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/").withCSRFToken

    "return 200 when authenticated" when {
      "empty page answers" in {
        val result = controller(SuccessfulAuth(), DisassociateUcrAnswers(ucr = None)).display(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(DisassociateUcr.form)
      }

      "existing page answers" in {
        val result = controller(SuccessfulAuth(), DisassociateUcrAnswers(ucr = Some(disassociation))).display(get)

        status(result) mustBe Status.OK
        contentAsHtml(result) mustBe page(DisassociateUcr.form.fill(disassociation))
      }
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }

  "POST" should {
    "return 200 when authenticated" in {
      val post = FakeRequest("POST", "/").withJsonBody(Json.toJson(disassociation)).withCSRFToken
      val result = controller(SuccessfulAuth(), DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DisassociateUCRSummaryController.display().url)
      theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers(ucr = Some(disassociation)))
    }

    "return 400 when invalid" in {
      implicit val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(SuccessfulAuth(), DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.BAD_REQUEST
      contentAsHtml(result) mustBe page(DisassociateUcr.form.bind(Map[String, String]()))
    }

    "return 403 when unauthenticated" in {
      val post = FakeRequest("POST", "/").withCSRFToken

      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).submit(post)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
