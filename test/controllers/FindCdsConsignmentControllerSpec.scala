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

import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.test.Helpers.status
import services.{MockCache, MockIleQueryCache}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.find_cds_consignment

import scala.concurrent.ExecutionContext.global

class FindCdsConsignmentControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache with ScalaFutures with IntegrationPatience {
  private val findCdsConsignmentPage = mock[find_cds_consignment]

  private def controller =
    new FindCdsConsignmentController(stubMessagesControllerComponents(), SuccessfulAuth(), cacheRepository, findCdsConsignmentPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(findCdsConsignmentPage)
    when(findCdsConsignmentPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(findCdsConsignmentPage)
    super.afterEach()
  }

  "FindCdsConsignmentController on displayPage" should {
    "return Ok (200) response" in {
      givenTheCacheIsEmpty()
      val result = controller.displayPage(getRequest)
      status(result) mustBe OK
    }
  }

  "Calling submitCdsConsignment" should {

    "return BAD_REQUEST when passing form with errors" in {
      val incorrectUCR = "123ABC-789456POIUYT"
      val result = controller.submitCdsConsignment(postRequest(Json.obj("ucr" -> incorrectUCR)))
      status(result) mustBe BAD_REQUEST
    }

    "return SEE_OTHER when passing form without errors" in {

      val correctUCR = "GB/123456789100-AB123"
      val result = controller.submitCdsConsignment(postRequest(Json.obj("ucr" -> correctUCR)))
      status(result) mustBe SEE_OTHER
    }
  }

}
