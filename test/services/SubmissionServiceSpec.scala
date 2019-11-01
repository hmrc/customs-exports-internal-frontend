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

import base.{MockCache, UnitSpec}
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.{AssociateUCRRequest, Consolidation, DisassociateDUCRRequest, ShutMUCRRequest}
import forms._
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends UnitSpec with MockCache with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val metrics = mock[MovementsMetrics]
  private val audit = mock[AuditService]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val service = new SubmissionService(cache, connector, audit, metrics)

  override def afterEach(): Unit = {
    reset(audit, connector, metrics, cache)
    super.afterEach()
  }

  "Submit Associate" should {

    val eori = "eori"
    val mucr = "123"
    val ucr = "321"

    "delegate to connector" in {

      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))

      val answers =
        AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)), Some(Summary("key" -> "value")))
      await(service.submit("pid", answers))

      theAssociationSubmitted mustBe AssociateUCRRequest("pid", "eori", mucr, ucr)
      theCacheUpserted mustBe Cache("pid", AssociateUcrAnswers(summary = Some(Summary(Map("key" -> "value")))))

      verify(audit).auditAssociate("eori", mucr, ucr, "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      intercept[RuntimeException] {
        await(service.submit("pid", answers))
      }

      theAssociationSubmitted mustBe AssociateUCRRequest("pid", "eori", mucr, ucr)
      verify(cache, never()).upsert(any[Cache])
      verify(audit).auditAssociate("eori", mucr, ucr, "Failed")
    }

    "handle missing eori" in {
      val answers = AssociateUcrAnswers(None, Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(cache)
      verifyZeroInteractions(audit)
    }

    "handle missing ucr" in {
      val answers = AssociateUcrAnswers(Some(eori), None, None)
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(cache)
      verifyZeroInteractions(audit)
    }

    def theAssociationSubmitted: AssociateUCRRequest = {
      val captor: ArgumentCaptor[AssociateUCRRequest] = ArgumentCaptor.forClass(classOf[AssociateUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Disassociate" should {
    "delegate to connector" when {
      "Disassociate MUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some("eori"), Some(DisassociateUcr(DisassociateKind.Mucr, None, Some("ucr"))))
        await(service.submit("pid", answers))

        theDisassociationSubmitted mustBe DisassociateDUCRRequest("pid", "eori", "ucr")
        verify(cache).removeByPid("pid")
        verify(audit).auditDisassociate("eori", "ucr", "Success")
      }

      "Disassociate DUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some("eori"), Some(DisassociateUcr(DisassociateKind.Ducr, Some("ucr"), None)))
        await(service.submit("pid", answers))

        theDisassociationSubmitted mustBe DisassociateDUCRRequest("pid", "eori", "ucr")
        verify(cache).removeByPid("pid")
        verify(audit).auditDisassociate("eori", "ucr", "Success")
      }
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = DisassociateUcrAnswers(Some("eori"), Some(DisassociateUcr(DisassociateKind.Mucr, None, Some("ucr"))))
      intercept[RuntimeException] {
        await(service.submit("pid", answers))
      }

      theDisassociationSubmitted mustBe DisassociateDUCRRequest("pid", "eori", "ucr")
      verify(cache, never()).removeByPid("pid")
      verify(audit).auditDisassociate("eori", "ucr", "Failed")
    }

    "handle missing eori" in {
      val answers = DisassociateUcrAnswers(None, Some(DisassociateUcr(DisassociateKind.Mucr, None, Some("ucr"))))
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(cache)
      verifyZeroInteractions(audit)
    }

    "handle missing ucr" when {
      "block is empty" in {
        val answers = DisassociateUcrAnswers(Some("eori"), None)
        intercept[Throwable] {
          await(service.submit("pid", answers))
        } mustBe ReturnToStartException

        verifyZeroInteractions(cache)
        verifyZeroInteractions(audit)
      }

      "missing fields" when {
        "Disassociate MUCR" in {
          val answers = DisassociateUcrAnswers(Some("eori"), Some(DisassociateUcr(DisassociateKind.Mucr, None, None)))
          intercept[Throwable] {
            await(service.submit("pid", answers))
          } mustBe ReturnToStartException

          verifyZeroInteractions(cache)
          verifyZeroInteractions(audit)
        }

        "Disassociate DUCR" in {
          val answers = DisassociateUcrAnswers(Some("eori"), Some(DisassociateUcr(DisassociateKind.Ducr, None, None)))
          intercept[Throwable] {
            await(service.submit("pid", answers))
          } mustBe ReturnToStartException

          verifyZeroInteractions(cache)
          verifyZeroInteractions(audit)
        }
      }
    }

    def theDisassociationSubmitted: DisassociateDUCRRequest = {
      val captor: ArgumentCaptor[DisassociateDUCRRequest] = ArgumentCaptor.forClass(classOf[DisassociateDUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit ShutMUCR" should {
    "delegate to connector" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))

      val answers = ShutMucrAnswers(Some("eori"), Some(ShutMucr("mucr")))
      await(service.submit("pid", answers))

      theShutMucrSubmitted mustBe ShutMUCRRequest("pid", "eori", "mucr")
      verify(cache).removeByPid("pid")
      verify(audit).auditShutMucr("eori", "mucr", "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = ShutMucrAnswers(Some("eori"), Some(ShutMucr("mucr")))
      intercept[RuntimeException] {
        await(service.submit("pid", answers))
      }

      theShutMucrSubmitted mustBe ShutMUCRRequest("pid", "eori", "mucr")
      verify(cache, never()).removeByPid("pid")
      verify(audit).auditShutMucr("eori", "mucr", "Failed")
    }

    "handle missing eori" in {
      val answers = ShutMucrAnswers(None, Some(ShutMucr("mucr")))
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(cache)
      verifyZeroInteractions(audit)
    }

    "handle missing mucr" in {
      val answers = ShutMucrAnswers(Some("eori"), None)
      intercept[Throwable] {
        await(service.submit("pid", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(cache)
      verifyZeroInteractions(audit)
    }

    def theShutMucrSubmitted: ShutMUCRRequest = {
      val captor: ArgumentCaptor[ShutMUCRRequest] = ArgumentCaptor.forClass(classOf[ShutMUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

}
