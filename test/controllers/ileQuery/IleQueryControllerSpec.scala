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

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.ControllerLayerSpec
import controllers.exchanges.Operator
import controllers.ileQuery.routes.IleQueryController
import handlers.ErrorHandler
import models.{now, UcrBlock}
import models.UcrType.Mucr
import models.cache.{Answers, Cache, IleQuery}
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.mvc.Results.InternalServerError
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.{MockCache, MockIleQueryCache}
import testdata.CommonTestData._
import uk.gov.hmrc.http.HttpResponse
import views.html._

import java.time.Instant
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache with ScalaFutures with IntegrationPatience {

  private val errorHandler = mock[ErrorHandler]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val ileQueryPage = mock[ile_query]
  private val loadingScreenPage = mock[loading_screen]
  private val ileQueryDucrResponsePage = mock[ile_query_ducr_response]
  private val ileQueryMucrResponsePage = mock[ile_query_mucr_response]
  private val consignmentNotFoundPage = mock[consignment_not_found_page]
  private val ileQueryTimeoutPage = mock[ile_query_timeout]

  private val controller = new IleQueryController(
    SuccessfulAuth(operator = Operator(providerId)),
    stubMessagesControllerComponents(),
    errorHandler,
    cacheRepository,
    ileQueryRepository,
    connector,
    ileQueryPage,
    loadingScreenPage,
    ileQueryDucrResponsePage,
    ileQueryMucrResponsePage,
    consignmentNotFoundPage,
    ileQueryTimeoutPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(errorHandler.defaultErrorTemplate(any(), any(), any())(any())).thenReturn(HtmlFormat.empty)
    when(ileQueryPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(loadingScreenPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryDucrResponsePage.apply(any[DucrInfo], any[Option[MucrInfo]])(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryMucrResponsePage.apply(any[MucrInfo], any[Option[MucrInfo]], any[Seq[UcrInfo]])(any(), any()))
      .thenReturn(HtmlFormat.empty)
    when(consignmentNotFoundPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryTimeoutPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(
      errorHandler,
      cacheRepository,
      ileQueryRepository,
      connector,
      ileQueryPage,
      loadingScreenPage,
      ileQueryDucrResponsePage,
      ileQueryMucrResponsePage,
      consignmentNotFoundPage,
      ileQueryTimeoutPage
    )

    super.afterEach()
  }

  private val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

  private val mucrInfo = MucrInfo(ucr = "mucr")
  private val parentMucrInfo = MucrInfo("parentMucr")
  private val successfulMucrResponseData = SuccessfulResponseExchangeData(queriedMucr = Some(mucrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulMucrResponseExchange =
    IleQueryResponseExchange(now, conversationId, "inventoryLinkingQueryResponse", Some(successfulMucrResponseData))

  private val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "DeclarationId")
  private val successfulDucrResponseData = SuccessfulResponseExchangeData(queriedDucr = Some(ducrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulDucrResponseExchange =
    IleQueryResponseExchange(now, conversationId, "inventoryLinkingQueryResponse", Some(successfulDucrResponseData))

  private val ucrNotFoundResponseData =
    UcrNotFoundResponseExchangeData(messageCode = "QUE", actionCode = "1", ucrBlock = Some(UcrBlock(ucr = "mucr", ucrType = Mucr)))
  private val ucrNotFoundResponseExchange =
    IleQueryResponseExchange(now, conversationId, "inventoryLinkingControlResponse", Some(ucrNotFoundResponseData))

  "IleQueryController on getConsignmentInformation" should {
    "call IleQueryRepository to find ILE Query cache document" in {
      when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
        .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
      when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
      val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
      when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

      controller.getConsignmentInformation("mucr")(request).futureValue

      verify(ileQueryRepository).findBySessionIdAndUcr(meq("sessionId"), meq("mucr"))
    }
  }

  "IleQueryController on getConsignmentInformation" when {

    "ileQuery cache is empty for the user" when {

      "provided with correct DUCR" should {
        val correctDucr = "9GB123456789012-QWERTY7890"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          controller.getConsignmentInformation(correctDucr)(request).futureValue

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe Answers.fakeEORI.get
          constructedIleQueryExchange.providerId mustBe providerId
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctDucr, ucrType = "D")
        }

        "call IleQueryRepository to insert cache document" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          controller.getConsignmentInformation(correctDucr)(request).futureValue

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insertOne(ileQueryCaptor.capture())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctDucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val result = controller.getConsignmentInformation(correctDucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(IleQueryController.getConsignmentInformation(correctDucr).url)
        }
      }

      "provided with correct MUCR" should {
        val correctMucr = "GB/123-QWERTY456"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          controller.getConsignmentInformation(correctMucr)(request).futureValue

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe Answers.fakeEORI.get
          constructedIleQueryExchange.providerId mustBe providerId
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctMucr, ucrType = "M")
        }

        "call IleQueryRepository to insert cache document" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          controller.getConsignmentInformation(correctMucr)(request).futureValue

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insertOne(ileQueryCaptor.capture())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctMucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val result = controller.getConsignmentInformation(correctMucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(IleQueryController.getConsignmentInformation(correctMucr).url)
        }
      }

      "provided with semantically incorrect UCR" should {
        val incorrectUCR = "123ABC-789456POIUYT"

        "return BadRequest (400) status" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))

          val result = controller.getConsignmentInformation(incorrectUCR)(request)

          status(result) mustBe BAD_REQUEST
        }

//        "return Find Consignment page, passing form with errors" in {
//          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
//
//          controller.getConsignmentInformation(incorrectUCR)(request).futureValue
//
//          val expectedForm = CdsOrChiefChoiceForm.form.fillAndValidate(Some(incorrectUCR))
//          verify(ileQueryPage).apply(meq(expectedForm))(any(), any())
//        }
      }
    }

    "ileQuery cache contains record for queried UCR" should {
      "call Backend Connector to fetch ILE Query Notifications" in {
        when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))

        when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
        val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
        when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

        controller.getConsignmentInformation("mucr")(request).futureValue

        verify(connector).fetchQueryNotifications(meq(conversationId), meq(providerId))(any())
      }
    }

    "ileQuery cache contains record for queried UCR" when {

      "Backend Connector returns OK (200) response with empty body" should {
        "return Loading page with 'refresh' header" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq.empty[IleQueryResponseExchange]), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          verify(loadingScreenPage).apply()(any(), any())
        }
      }

      "Backend Connector returns OK (200) response with Notifications in body" should {
        "call IleQueryRepository to remove cache document" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }
      }

      "Backend Connector returns OK (200) response with Notifications in body" when {

        "Notification has UcrNotFoundResponseExchangeData" should {
          "return ConsignmentNotFound page" in {
            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(ucrNotFoundResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentInformation("mucr")(request)

            status(result) mustBe OK
            verify(consignmentNotFoundPage).apply(meq("mucr"))(any(), any())
          }
        }

        "Notification has SuccessfulResponseExchangeData with 'queriedDucr'" should {

          "call CacheRepository to upsert queried DUCR" in {
            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulDucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            controller.getConsignmentInformation("ducr")(request).futureValue

            val cacheCaptor = newCacheCaptor
            verify(cacheRepository).upsert(cacheCaptor.capture())
            val cacheUpserted = cacheCaptor.getValue

            cacheUpserted.providerId mustBe providerId
            cacheUpserted.queryUcr.get mustBe UcrBlock(ucr = "ducr", ucrType = "D")
            cacheUpserted.answers mustBe None
          }

          "return DUCR query response page" in {
            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulDucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentInformation("ducr")(request)

            status(result) mustBe OK
            val optMucrInfoCaptor = newOptionalMucrInfoCaptor
            verify(ileQueryDucrResponsePage).apply(meq(ducrInfo), optMucrInfoCaptor.capture())(any(), any())
            optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
            verifyZeroInteractions(ileQueryMucrResponsePage)
          }
        }

        "Notification has SuccessfulResponseExchangeData with 'queriedMucr'" should {

          "call CacheRepository to upsert queried MUCR" in {
            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            controller.getConsignmentInformation("mucr")(request).futureValue

            val cacheCaptor = newCacheCaptor
            verify(cacheRepository).upsert(cacheCaptor.capture())
            val cacheUpserted = cacheCaptor.getValue

            cacheUpserted.providerId mustBe providerId
            cacheUpserted.queryUcr.get mustBe UcrBlock(ucr = "mucr", ucrType = "M")
            cacheUpserted.answers mustBe None
          }

          "return MUCR query response page" in {
            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentInformation("mucr")(request)

            status(result) mustBe OK
            val optMucrInfoCaptor = newOptionalMucrInfoCaptor
            verify(ileQueryMucrResponsePage).apply(meq(mucrInfo), optMucrInfoCaptor.capture(), meq(Seq.empty))(any(), any())
            optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
            verifyZeroInteractions(ileQueryDucrResponsePage)
          }
        }
      }

      "Backend Connector returns a response other than OK (200)" should {

        "call IleQueryRepository to remove cache document" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))

          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = SERVICE_UNAVAILABLE, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          controller.getConsignmentInformation("mucr")(request).futureValue

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return InternalServerError response" in {
          when(errorHandler.internalServerError(any())).thenReturn(InternalServerError)

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))

          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = SERVICE_UNAVAILABLE, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }

      "Backend Connector returns FailedDependency (424) response" should {

        "call IleQueryRepository to remove cache document" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = FAILED_DEPENDENCY, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          controller.getConsignmentInformation("mucr")(request).futureValue

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return timeout page" in {
          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))

          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = FAILED_DEPENDENCY, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
        }
      }
    }
  }

  private def newOptionalMucrInfoCaptor: ArgumentCaptor[Option[MucrInfo]] = ArgumentCaptor.forClass(classOf[Option[MucrInfo]])
  private def newIleQueryExchangeCaptor: ArgumentCaptor[IleQueryExchange] = ArgumentCaptor.forClass(classOf[IleQueryExchange])
  private def newIleQueryCaptor: ArgumentCaptor[IleQuery] = ArgumentCaptor.forClass(classOf[IleQuery])
  private def newCacheCaptor: ArgumentCaptor[Cache] = ArgumentCaptor.forClass(classOf[Cache])

  def exampleIleQuery(
    sessionId: String = "sessionId",
    ucr: String = correctUcr,
    conversationId: String = conversationId,
    createdAt: Instant = now
  ): IleQuery =
    IleQuery(sessionId = sessionId, ucr = ucr, conversationId = conversationId, createdAt = createdAt)
}
