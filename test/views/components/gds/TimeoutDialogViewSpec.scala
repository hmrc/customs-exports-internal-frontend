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

package views.components.gds

import base.Injector
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.components.gds.timeoutDialog

import scala.concurrent.duration.*

class TimeoutDialogViewSpec extends ViewSpec with Injector {

  private implicit val request: FakeRequest[?] = FakeRequest()

  private val view = instanceOf[timeoutDialog]

  "timeoutDialog component" should {
    "render service name with link to 'find consignment' page" in {
      val dialog = view("keepAliveUrl", 2.seconds, 1.second).getElementById("timeout-dialog")
      dialog.tagName mustBe "div"
      dialog.attr("data-timeout") mustBe "2"
      dialog.attr("data-countdown") mustBe "1"
      dialog.attr("data-sign-out-url") mustBe "/sign-out?signOutReason=SessionTimeout"
    }
  }
}
