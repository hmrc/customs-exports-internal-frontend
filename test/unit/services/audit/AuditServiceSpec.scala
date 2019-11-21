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

import base.UnitSpec
import connectors.exchanges.{ArrivalExchange, MovementDetailsExchange}
import forms._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import services.audit.EventData._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val headerCarrier = HeaderCarrier()

  private val mockAuditConnector = mock[AuditConnector]
  private val service = Mockito.spy(new AuditService(mockAuditConnector, "appName"))

  override def beforeEach(): Unit =
    when(mockAuditConnector.sendEvent(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(AuditResult.Success))

  override def afterEach(): Unit = reset(mockAuditConnector)

  "AuditService" should {
    "audit Shut a Mucr data" in {
      val dataToAudit = Map(providerId.toString -> "providerId", mucr.toString -> "mucr", submissionResult.toString -> "200")
      service.auditShutMucr("providerId", "mucr", "200")
      verify(service).audit(AuditTypes.AuditShutMucr, dataToAudit)
    }

    "audit an association" in {
      val dataToAudit = Map(providerId.toString -> "providerId", mucr.toString -> "mucr", ducr.toString -> "ducr", submissionResult.toString -> "200")
      service.auditAssociate("providerId", "mucr", "ducr", "200")
      verify(service).audit(AuditTypes.AuditAssociate, dataToAudit)
    }

    "audit a disassociation" in {
      val dataToAudit = Map(providerId.toString -> "providerId", ducr.toString -> "ducr", submissionResult.toString -> "200")
      service.auditDisassociate("providerId", "ducr", "200")
      verify(service).audit(AuditTypes.AuditDisassociate, dataToAudit)
    }

    "audit a movement" in {
      val dataToAudit = Map(
        EventData.movementReference.toString -> "ref",
        EventData.providerId.toString -> "122343",
        EventData.messageCode.toString -> "EAL",
        EventData.ucr.toString -> "UCR",
        EventData.ucrType.toString -> "D",
        EventData.submissionResult.toString -> "200"
      )
      val data =
        ArrivalExchange(
          eori = "GB12345678",
          providerId = "122343",
          consignmentReference = ConsignmentReferences("UCR", "D"),
          movementDetails = Some(MovementDetailsExchange("dateTime")),
          location = Location("location"),
          arrivalReference = ArrivalReference(Some("ref"))
        )
      service.auditMovements(data, "200", AuditTypes.AuditArrival)
      verify(service).audit(AuditTypes.AuditArrival, dataToAudit)
    }
  }
}
