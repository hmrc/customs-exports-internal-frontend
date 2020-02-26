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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.associateucr.associate_ucr_confirmation

import scala.concurrent.ExecutionContext.Implicits.global

class AssociateUCRConfirmationControllerSpec extends ControllerLayerSpec with MockitoSugar {

  private val page = mock[associate_ucr_confirmation]

  private def controller(auth: AuthenticatedAction) =
    new AssociateUCRConfirmationController(auth, stubMessagesControllerComponents(), page)

  "GET" should {
    when(page.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    implicit val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      val result = controller(SuccessfulAuth()).display(get.withFlash(FlashKeys.CONSOLIDATION_KIND -> "kind", FlashKeys.UCR -> "123"))

      status(result) mustBe Status.OK
      contentAsHtml(result) mustBe page()
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).display(get)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}
