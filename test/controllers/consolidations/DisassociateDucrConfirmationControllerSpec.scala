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

package controllers.consolidations

import controllers.ControllerLayerSpec
import controllers.actions.AuthenticatedAction
import models.cache.{Answers, DisassociateUcrAnswers}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.disassociate_ducr_confirmation

import scala.concurrent.ExecutionContext.Implicits.global

class DisassociateDucrConfirmationControllerSpec extends ControllerLayerSpec {

  private val page = new disassociate_ducr_confirmation(main_template)

  private def controller(auth: AuthenticatedAction, existingAnswers: Answers) =
    new DisassociateDucrConfirmationController(auth, ValidJourney(existingAnswers), stubMessagesControllerComponents(), page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      val result = controller(SuccessfulAuth(), DisassociateUcrAnswers()).display(get)

      status(result) mustBe Status.OK
      contentAsHtml(result) mustBe page()
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth, DisassociateUcrAnswers()).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
