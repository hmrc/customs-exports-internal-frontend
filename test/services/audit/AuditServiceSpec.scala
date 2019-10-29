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

package services.audit

import base.BaseSpec
import forms._
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import services.audit.EventData._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends BaseSpec with BeforeAndAfterEach with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val headerCarrier = HeaderCarrier()

  val mockAuditConnector = mock[AuditConnector]
  val spyAuditService = Mockito.spy(new AuditService(mockAuditConnector, "appName"))

  override def beforeEach(): Unit =
    when(mockAuditConnector.sendEvent(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(AuditResult.Success))

  override def afterEach(): Unit = reset(mockAuditConnector)

  "AuditService" should {
    "audit Shut a Mucr data" in {
      val dataToAudit = Map(eori.toString -> "eori", mucr.toString -> "mucr", submissionResult.toString -> "200")
      spyAuditService.auditShutMucr("eori", "mucr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditShutMucr, dataToAudit)
    }

    "audit an association" in {
      val dataToAudit = Map(eori.toString -> "eori", mucr.toString -> "mucr", ducr.toString -> "ducr", submissionResult.toString -> "200")
      spyAuditService.auditAssociate("eori", "mucr", "ducr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditAssociate, dataToAudit)
    }

    "audit a disassociation" in {
      val dataToAudit = Map(eori.toString -> "eori", ducr.toString -> "ducr", submissionResult.toString -> "200")
      spyAuditService.auditDisassociate("eori", "ducr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditDisassociate, dataToAudit)
    }

    "audit a movement" in {
      val dataToAudit = Map(
        EventData.movementReference.toString -> "",
        EventData.eori.toString -> "GB12345678",
        EventData.messageCode.toString -> "EAL",
        EventData.ucr.toString -> "UCR",
        EventData.ucrType.toString -> "D",
        EventData.submissionResult.toString -> "200"
      )
      val data =
        MovementRequest(
          eori = "GB12345678",
          providerId = "122343",
          choice = MovementType.Arrival,
          consignmentReference = ConsignmentReferences("UCR", "D"),
          movementDetails = MovementDetailsRequest("dateTime")
        )
      spyAuditService.auditMovements(data, "200", AuditTypes.AuditArrival)
      verify(spyAuditService).audit(AuditTypes.AuditArrival, dataToAudit)
    }
  }
}
