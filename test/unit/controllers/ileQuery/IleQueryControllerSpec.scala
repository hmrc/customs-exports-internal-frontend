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

import config.ErrorHandler
import connectors.CustomsDeclareExportsMovementsConnector
import controllers.ControllerLayerSpec
import models.cache.IleQuery
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockIleQueryCache
import uk.gov.hmrc.http.HttpResponse
import views.html._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends ControllerLayerSpec with MockIleQueryCache {

  private val errorHandler = mock[ErrorHandler]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val ileQueryPage = mock[ile_query]
  private val loadingScreenPage = mock[loading_screen]
  private val ileQueryDucrResponsePage = mock[ile_query_ducr_response]
  private val ileQueryMucrResponsePage = mock[ile_query_mucr_response]

  private val controller: IleQueryController = new IleQueryController(
    SuccessfulAuth(),
    stubMessagesControllerComponents(),
    errorHandler,
    ileQueryRepository,
    connector,
    ileQueryPage,
    loadingScreenPage,
    ileQueryDucrResponsePage,
    ileQueryMucrResponsePage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(errorHandler.standardErrorTemplate()(any())).thenReturn(HtmlFormat.empty)
    when(ileQueryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(loadingScreenPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(errorHandler, connector, ileQueryPage, loadingScreenPage)

    super.afterEach()
  }

  "Ile Query Controller" should {

    "return 200 (OK)" when {

      "display query form method is invoked" in {

        val result = controller.displayQueryForm()(getRequest)

        status(result) mustBe OK
      }

      "submit query method is invoked and notifications are available" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val result = controller.submitQuery("ucr")(getRequest)

        status(result) mustBe OK
      }

      "submit query method is invoked and there is no notifications available yet" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))

        val result = controller.submitQuery("ucr")(getRequest)

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "query form is incorrect" in {}

      "connector returned different status than OK, NO_CONTENT, GATEWAY_TIMEOUT" in {}

      "ucr is incorrect during submitting ile query" in {}
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {

      "there is a timeout during waiting for notifications" in {}
    }

    "return 303 (SEE_OTHER)" when {

      "correct form has been submitted" in {}

      "correct ucr has been submitted" in {}
    }
  }
}
