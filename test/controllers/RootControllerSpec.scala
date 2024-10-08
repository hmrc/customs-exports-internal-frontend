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

package controllers

import controllers.ileQuery.routes.FindConsignmentController
import play.api.test.Helpers._

class RootControllerSpec extends ControllerLayerSpec {

  private val controller = new RootController(stubMessagesControllerComponents())

  "Root Controller" should {
    "return 303 (SEE_OTHER)" when {
      "redirect user to the choice page" in {
        val result = controller.displayPage(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FindConsignmentController.displayQueryForm.url)
      }
    }
  }
}
