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

import base.UnitSpec
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.{AssociateUCRRequest, Consolidation, DisassociateDUCRRequest}
import forms.{AssociateKind, AssociateUcr, MucrOptions}
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, DisassociateUcrAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import repositories.MovementRepository
import services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val metrics = mock[MovementsMetrics]
  private val audit = mock[AuditService]
  private val repository = mock[MovementRepository]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val service = new SubmissionService(repository, connector, audit, metrics)

  override def afterEach(): Unit = {
    reset(audit, connector, metrics, repository)
    super.afterEach()
  }

  "Submit Associate" should {

    val eori = "eori"
    val mucr = "123"
    val ucr = "321"

    "delegate to connector" in {

      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
      given(repository.removeByPid(anyString())).willReturn(Future.successful((): Unit))

      val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      await(service.submit("pid", answers))

      theAssociationSubmitted mustBe AssociateUCRRequest("pid", "eori", mucr, ucr)
      verify(repository).removeByPid("pid")
      verify(audit).auditAssociate("eori", mucr, ucr, "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      intercept[RuntimeException] {
        await(service.submit("pid", answers))
      }

      theAssociationSubmitted mustBe AssociateUCRRequest("pid", "eori", mucr, ucr)
      verify(repository, never()).removeByPid("pid")
      verify(audit).auditAssociate("eori", mucr, ucr, "Failed")
    }

    "handle missing eori" in {
      val answers = AssociateUcrAnswers(None, Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(repository)
      verifyZeroInteractions(audit)
    }

    "handle missing ucr" in {
      val answers = AssociateUcrAnswers(Some(eori), None, None)
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(repository)
      verifyZeroInteractions(audit)
    }

    def theAssociationSubmitted: AssociateUCRRequest = {
      val captor: ArgumentCaptor[AssociateUCRRequest] = ArgumentCaptor.forClass(classOf[AssociateUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Disassociate" should {
    "delegate to connector" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
      given(repository.removeByPid(anyString())).willReturn(Future.successful((): Unit))

      val answers = DisassociateUcrAnswers(Some("eori"), Some("ucr"))
      await(service.submit("pid", answers))

      theDisassociationSubmitted mustBe DisassociateDUCRRequest("pid", "eori", "ucr")
      verify(repository).removeByPid("pid")
      verify(audit).auditDisassociate("eori", "ucr", "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = DisassociateUcrAnswers(Some("eori"), Some("ucr"))
      intercept[RuntimeException] {
        await(service.submit("pid", answers))
      }

      theDisassociationSubmitted mustBe DisassociateDUCRRequest("pid", "eori", "ucr")
      verify(repository, never()).removeByPid("pid")
      verify(audit).auditDisassociate("eori", "ucr", "Failed")
    }

    "handle missing eori" in {
      val answers = DisassociateUcrAnswers(None, Some("ucr"))
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(repository)
      verifyZeroInteractions(audit)
    }

    "handle missing ucr" in {
      val answers = DisassociateUcrAnswers(Some("eori"), None)
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(repository)
      verifyZeroInteractions(audit)
    }

    def theDisassociationSubmitted: DisassociateDUCRRequest = {
      val captor: ArgumentCaptor[DisassociateDUCRRequest] = ArgumentCaptor.forClass(classOf[DisassociateDUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

}
