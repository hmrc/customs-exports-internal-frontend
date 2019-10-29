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

package services

import base._
import models.cache.{Cache, JourneyType}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.Helpers._
import services.audit.{AuditService, AuditTypes}
import testdata.MovementsTestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec
    extends UnitSpec with ScalaFutures with MovementsMetricsStub with MockCustomsExportsMovement with MetricsMatchers with BeforeAndAfterEach
    with MockMovementsRepository {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val headerCarrierMock = mock[HeaderCarrier]

  val mockAuditService = mock[AuditService]

  val submissionService =
    new SubmissionService(mockCustomsExportsMovementConnector, mockAuditService, movementsMetricsStub)

  override def afterEach(): Unit = {
    reset(mockMovementsRepository, mockCustomsExportsMovementConnector, mockAuditService)
    super.afterEach()
  }

  private def requestAcceptedTest(block: => Any): Any = {
    when(mockCustomsExportsMovementConnector.sendArrivalDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(mockCustomsExportsMovementConnector.sendDepartureDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

    block
  }

  "SubmissionService on submitMovementRequest" when {

    "submitting Arrival" should {

      "return response from CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        val answers = MovementsTestData.validMovementAnswers(JourneyType.ARRIVE)
        when(mockMovementsRepository.findByPid(any()))
          .thenReturn(Future.successful(Some(Cache("pid", answers))))

        val CustomHttpResponseCode = 123
        when(mockCustomsExportsMovementConnector.sendArrivalDeclaration(any())(any()))
          .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

        await(submissionService.submitMovementRequest("pid", answers).map(_._2)) must equal(CustomHttpResponseCode)
        verify(mockAuditService).auditMovements(any(), any(), ArgumentMatchers.eq(AuditTypes.AuditArrival))(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(answers))(any())
      }
    }
  }
}
