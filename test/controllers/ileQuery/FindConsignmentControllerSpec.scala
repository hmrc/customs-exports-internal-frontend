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
import controllers.exchanges.AuthenticatedRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, *}
import play.twirl.api.HtmlFormat
import services.MockCache
import views.html.ile_query

class FindConsignmentControllerSpec extends ControllerLayerSpec with MockCache {

  private val ileQueryPage = mock[ile_query]

  private val controller: FindConsignmentController =
    new FindConsignmentController(SuccessfulAuth(), stubMessagesControllerComponents(), ileQueryPage, cacheRepository)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQueryPage)

    when(ileQueryPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(ileQueryPage)

    super.afterEach()
  }

  "FindConsignmentController on displayQueryForm" should {
    "return Ok status (200)" in {
      val authenticatedRequest = AuthenticatedRequest(operator, FakeRequest())
      val result = controller.displayQueryForm(authenticatedRequest)
      verify(cacheRepository).removeByProviderId(authenticatedRequest.providerId)
      status(result) mustBe OK
    }
  }

}
