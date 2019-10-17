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

package uk.gov.hmrc.customsexportsinternalfrontend.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customsexportsinternalfrontend.controllers.actions.{AuthenticatedAction, Verify}
import uk.gov.hmrc.customsexportsinternalfrontend.views.html.hello_world
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class HelloWorldControllerSpec extends ControllerLayerSpec {

  private val helloWorldPage = aRealInstanceOf[hello_world]

  private def controller(auth: AuthenticatedAction) =
    new HelloWorldController(new Verify(auth), stubMessagesControllerComponents(), helloWorldPage)

  "GET /" should {
    val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      val result = controller(SuccessfulAuth()).helloWorld(get)
      status(result) mustBe Status.OK
    }

    "return 403 when unauthenticated" in {
      val result = controller(UnsuccessfulAuth).helloWorld(get)
      status(result) mustBe Status.FORBIDDEN
    }
  }
}
