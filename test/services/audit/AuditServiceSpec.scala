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

package services.audit

import base.UnitSpec
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsValue, Json}
import testdata.CommonTestData.providerId
import testdata.{CommonTestData, MovementsTestData}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val auditConnector = mock[AuditConnector]
  private val service = new AuditService(auditConnector, "appName")

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(auditConnector.sendEvent(any())(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))
    when(auditConnector.sendExtendedEvent(any())(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))
  }

  override def afterEach(): Unit = {
    reset(auditConnector)

    super.afterEach()
  }

  private def auditTagsPayload(auditType: String): Map[String, String] = Map(
    "clientIP" -> "-",
    "path" -> s"customs-declare-exports/$auditType/full-payload",
    "X-Session-ID" -> "-",
    "Akamai-Reputation" -> "-",
    "X-Request-ID" -> "-",
    "deviceID" -> "-",
    "clientPort" -> "-",
    "transactionName" -> s"export-declaration-${auditType}-payload-request"
  )

  private def auditTagsResult(auditType: String): Map[String, String] = Map(
    "clientIP" -> "-",
    "path" -> s"customs-declare-exports/$auditType",
    "X-Session-ID" -> "-",
    "Akamai-Reputation" -> "-",
    "X-Request-ID" -> "-",
    "deviceID" -> "-",
    "clientPort" -> "-",
    "transactionName" -> s"export-declaration-${auditType}-request"
  )

  private def sentExtendedDataEvent: ExtendedDataEvent = {
    val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(auditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  private def sentDataEvent: DataEvent = {
    val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
    verify(auditConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  "AuditService" should {

    "call AuditConnector sendExtendedEvent method with correct data when auditing submission payload" when {

      "used for Arrival" in {
        val answers = MovementsTestData.validArrivalAnswers

        val auditTags = auditTagsPayload("arrival")
        val auditDetail: JsValue = Json.obj(
          "pid" -> providerId,
          "ConsignmentReferences" -> answers.consignmentReferences,
          "Location" -> answers.location,
          "MovementDetails" -> answers.arrivalDetails
        )
        val expectedExtendedDataEvent =
          ExtendedDataEvent(auditSource = "appName", auditType = "arrival", tags = auditTags, detail = auditDetail)

        service.auditAllPagesUserInput(providerId, answers)

        val actualExtendedDataEvent = sentExtendedDataEvent
        actualExtendedDataEvent.auditSource mustBe expectedExtendedDataEvent.auditSource
        actualExtendedDataEvent.auditType mustBe expectedExtendedDataEvent.auditType
        actualExtendedDataEvent.tags mustBe expectedExtendedDataEvent.tags
        actualExtendedDataEvent.detail mustBe expectedExtendedDataEvent.detail
      }

      "used for Retrospective Arrival" in {
        val answers = MovementsTestData.validRetrospectiveArrivalAnswers

        val auditTags = auditTagsPayload("retrospective-arrival")
        val auditDetail: JsValue =
          Json.obj("pid" -> providerId, "ConsignmentReferences" -> answers.consignmentReferences, "Location" -> answers.location)
        val expectedExtendedDataEvent =
          ExtendedDataEvent(auditSource = "appName", auditType = "retrospective-arrival", tags = auditTags, detail = auditDetail)

        service.auditAllPagesUserInput(providerId, answers)

        val actualExtendedDataEvent = sentExtendedDataEvent
        actualExtendedDataEvent.auditSource mustBe expectedExtendedDataEvent.auditSource
        actualExtendedDataEvent.auditType mustBe expectedExtendedDataEvent.auditType
        actualExtendedDataEvent.tags mustBe expectedExtendedDataEvent.tags
        actualExtendedDataEvent.detail mustBe expectedExtendedDataEvent.detail
      }

      "used for Departure" in {
        val answers = MovementsTestData.validDepartureAnswers

        val auditTags = auditTagsPayload("departure")
        val auditDetail: JsValue = Json.obj(
          "pid" -> providerId,
          "ConsignmentReferences" -> answers.consignmentReferences,
          "Location" -> answers.location,
          "MovementDetails" -> answers.departureDetails,
          "Transport" -> answers.transport
        )
        val expectedExtendedDataEvent =
          ExtendedDataEvent(auditSource = "appName", auditType = "departure", tags = auditTags, detail = auditDetail)

        service.auditAllPagesUserInput(providerId, answers)

        val actualExtendedDataEvent = sentExtendedDataEvent
        actualExtendedDataEvent.auditSource mustBe expectedExtendedDataEvent.auditSource
        actualExtendedDataEvent.auditType mustBe expectedExtendedDataEvent.auditType
        actualExtendedDataEvent.tags mustBe expectedExtendedDataEvent.tags
        actualExtendedDataEvent.detail mustBe expectedExtendedDataEvent.detail
      }
    }

    "call AuditConnector sendEvent method with correct data when auditing submission result" when {

      "used for Shut a Mucr" in {
        val auditTags = auditTagsResult("shut-mucr")
        val auditDetail = Map("pid" -> providerId, "mucr" -> "mucr", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "shut-mucr", tags = auditTags, detail = auditDetail)

        service.auditShutMucr(providerId, "mucr", "Success")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Association" in {
        val auditTags = auditTagsResult("associate")
        val auditDetail =
          Map("pid" -> providerId, "mucr" -> "mucr", "ducr" -> "ducr", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "associate", tags = auditTags, detail = auditDetail)

        service.auditAssociate(providerId, "mucr", "ducr", "Success")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Disassociation" in {
        val auditTags = auditTagsResult("disassociate")
        val auditDetail = Map("pid" -> providerId, "ucr" -> "ucr", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "disassociate", tags = auditTags, detail = auditDetail)

        service.auditDisassociate(providerId, "ucr", "Success")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Arrival" in {
        val auditTags = auditTagsResult("arrival")
        val auditDetail =
          Map("pid" -> providerId, "messageCode" -> "EAL", "ucr" -> CommonTestData.correctUcr, "ucrType" -> "D", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "arrival", tags = auditTags, detail = auditDetail)

        val data = MovementsTestData.validArrivalExchange

        service.auditMovements(data, "Success", AuditType.AuditArrival)

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Retrospective Arrival" in {
        val auditTags = auditTagsResult("retrospective-arrival")
        val auditDetail =
          Map("pid" -> providerId, "messageCode" -> "RET", "ucr" -> CommonTestData.correctUcr, "ucrType" -> "D", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "retrospective-arrival", tags = auditTags, detail = auditDetail)

        val data = MovementsTestData.validRetrospectiveArrivalExchange

        service.auditMovements(data, "Success", AuditType.AuditRetrospectiveArrival)

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Departure" in {
        val auditTags = auditTagsResult("departure")
        val auditDetail =
          Map("pid" -> providerId, "messageCode" -> "EDL", "ucr" -> CommonTestData.correctUcr, "ucrType" -> "D", "submissionResult" -> "Success")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = "departure", tags = auditTags, detail = auditDetail)

        val data = MovementsTestData.validDepartureExchange

        service.auditMovements(data, "Success", AuditType.AuditDeparture)

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }
    }
  }
}
