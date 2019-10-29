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

package views.disassociate_ucr

import controllers.storage.FlashKeys
import play.api.mvc.{AnyContentAsEmpty, Flash, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociate_ducr_confirmation

class DisassociateDucrConfirmationViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val page = new disassociate_ducr_confirmation(main_template)

  "View" should {
    implicit val flash: Flash = Flash(Map(FlashKeys.UCR -> "ucr"))
    val view = page()

    "render title" in {
      view.getTitle must containMessage("disassociateDucr.confirmation.tab.heading", "ucr")
    }

    "render confirmation dialogue" in {
      view.getElementById("highlight-box-heading") must containMessage("disassociateDucr.confirmation.heading", "ucr")
    }
  }

}
