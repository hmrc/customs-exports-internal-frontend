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
import controllers.storage.FlashKeys
import models.cache.{Answers, AssociateUcrAnswers, Summary}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.associate_ucr_confirmation

import scala.concurrent.ExecutionContext.Implicits.global

class AssociateUcrConfirmationControllerSpec extends ControllerLayerSpec {

  private val page = new associate_ucr_confirmation(main_template)

  private def controller(auth: AuthenticatedAction, existingAnswers: Answers) =
    new AssociateUcrConfirmationController(auth, ValidJourney(existingAnswers), stubMessagesControllerComponents(), page)

  "GET" should {
    implicit val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      val result = controller(
        SuccessfulAuth(),
        AssociateUcrAnswers(summary = Some(Summary(Map(FlashKeys.CONSOLIDATION_KIND -> "kind", FlashKeys.UCR -> "ucr"))))
      ).display(get)

      status(result) mustBe Status.OK
      contentAsHtml(result) mustBe page("kind", "ucr")
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth, AssociateUcrAnswers()).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
