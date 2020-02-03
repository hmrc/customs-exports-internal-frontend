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

package controllers.ileQuery

import controllers.ControllerLayerSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers.{status, _}
import play.twirl.api.HtmlFormat
import views.html.consignment_not_found_page

import scala.concurrent.ExecutionContext.global

class ConsignmentReferenceNotFoundControllerSpec extends ControllerLayerSpec {

  private val consignmentNotFoundPage = mock[consignment_not_found_page]

  private val controller: ConsignmentNotFoundController =
    new ConsignmentNotFoundController(SuccessfulAuth(), stubMessagesControllerComponents(), consignmentNotFoundPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(consignmentNotFoundPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(consignmentNotFoundPage)

    super.afterEach()
  }

  "Consignment Not Found Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage("SOME_UCR")(getRequest)

        status(result) mustBe OK
      }
    }
  }

}
