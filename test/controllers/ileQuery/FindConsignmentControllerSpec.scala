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

package controllers.ileQuery

import controllers.ControllerLayerSpec
import controllers.routes.ManageChiefConsignmentController
import controllers.ileQuery.routes.IleQueryController
import org.mockito.ArgumentMatchers.any

import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers.{status, _}
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.correctUcr
import views.html.ile_query

class FindConsignmentControllerSpec extends ControllerLayerSpec {

  private val ileQueryPage = mock[ile_query]

  private val controller: FindConsignmentController =
    new FindConsignmentController(SuccessfulAuth(), stubMessagesControllerComponents(), ileQueryPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQueryPage)

    when(ileQueryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(ileQueryPage)

    super.afterEach()
  }

  "FindConsignmentController on displayQueryForm" should {
    "return Ok status (200)" in {
      val result = controller.displayQueryForm(getRequest)

      status(result) mustBe OK
    }
  }

  "FindConsignmentController on submitQueryForm" when {

    "provide with correct form" should {
      val cdsForm = Json.obj(("ucr", JsString(correctUcr)), ("isIleQuery", JsString("cds")))
      val chiefForm = Json.obj(("isIleQuery", JsString("chief")))
      "return SeeOther status (303)" in {
        val result = controller.submitQueryForm()(postRequest(cdsForm))

        status(result) mustBe SEE_OTHER
      }

      "redirect to Consignment Details page" in {
        val result = controller.submitQueryForm()(postRequest(cdsForm))

        redirectLocation(result).get mustBe IleQueryController.getConsignmentInformation(correctUcr).url
      }

      "redirect to Manage a CHIEF UCR page" in {
        val result = controller.submitQueryForm()(postRequest(chiefForm))

        redirectLocation(result).get mustBe ManageChiefConsignmentController.displayPage.url
      }
    }

    "provided with incorrect form" should {
      "return BadRequest status (400)" in {
        val incorrectForm = JsString("1234")

        val result = controller.submitQueryForm()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
