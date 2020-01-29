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

import java.time.Instant

import config.ErrorHandler
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.ControllerLayerSpec
import models.UcrBlock
import models.cache.IleQuery
import models.notifications.queries.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import models.notifications.queries.{DucrInfo, IleQueryResponseExchange, MucrInfo}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.{MockCache, MockIleQueryCache}
import testdata.CommonTestData.correctUcr
import uk.gov.hmrc.http.HttpResponse
import views.html._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache {

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
    cacheRepository,
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
    when(ileQueryDucrResponsePage.apply(any[DucrInfo])(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryMucrResponsePage.apply(any[MucrInfo])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(errorHandler, connector, ileQueryPage, loadingScreenPage, ileQueryDucrResponsePage, ileQueryMucrResponsePage)

    super.afterEach()
  }

  "Ile Query Controller" should {

    "return 200 (OK)" when {

      "display query form method is invoked" in {

        val result = controller.displayQueryForm()(getRequest)

        status(result) mustBe OK
      }

      "submit mucr query method is invoked and notifications are available" in {

        val mucrInfo = MucrInfo("mucr")
        val responseData = SuccessfulResponseExchangeData(queriedMucr = Some(mucrInfo))
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("mucr")(request)

        status(result) mustBe OK
        verify(ileQueryMucrResponsePage).apply(meq(mucrInfo))(any(), any())

        theCacheUpserted.queryUcr mustBe Some(UcrBlock("mucr", "M"))
      }

      "submit ducr query method is invoked and notifications are available" in {

        val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "decId")
        val responseData = SuccessfulResponseExchangeData(queriedDucr = Some(ducrInfo))
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ducr")(request)

        status(result) mustBe OK
        verify(ileQueryDucrResponsePage).apply(meq(ducrInfo))(any(), any())

        theCacheUpserted.queryUcr mustBe Some(UcrBlock("ducr", "D"))
      }

      "submit query method is invoked and neither mucr or ducr info available" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(Seq.empty[IleQueryResponseExchange])))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("mucr")(request)

        status(result) mustBe OK
        verify(loadingScreenPage).apply()(any(), any())

        verify(cacheRepository, never).upsert(any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "query form is incorrect" in {

        val incorrectForm = JsString("1234")

        val result = controller.submitQueryForm()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }

      "ucr is incorrect during submitting ile query" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(None))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {

      "there is a timeout during waiting for notifications" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(GATEWAY_TIMEOUT)))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "connector returned different status than OK" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 303 (SEE_OTHER)" when {

      "correct form has been submitted" in {

        val correctForm = Json.obj(("ucr", JsString(correctUcr)))

        val result = controller.submitQueryForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "correct ucr has been submitted" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(None))
        when(connector.submit(any[IleQueryExchange])(any()))
          .thenReturn(Future.successful("convId"))
        when(ileQueryRepository.insert(any())(any()))
          .thenReturn(Future.successful(dummyWriteResultSuccess))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery(correctUcr)(request)

        status(result) mustBe SEE_OTHER
      }
    }
  }
}
