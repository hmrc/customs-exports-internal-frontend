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

package services

import base.UnitSpec
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.*
import forms.*
import metrics.MovementsMetricsStub
import models.cache.{AssociateUcrAnswers, DisassociateUcrAnswers, MovementAnswers, ShutMucrAnswers}
import models.{ReturnToStartException, UcrType}
import org.mockito.ArgumentMatchers.{any, anyString, eq as meq}
import org.mockito.Mockito.*
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.*
import repositories.CacheRepository
import services.audit.{AuditService, AuditType}
import testdata.CommonTestData.*
import testdata.MovementsTestData.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends UnitSpec with MovementsMetricsStub with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val audit = mock[AuditService]
  private val repository = mock[CacheRepository]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val movementBuilder = mock[MovementBuilder]
  private val service = new SubmissionService(repository, connector, audit, movementsMetricsStub, movementBuilder)

  private val eori = validEori
  private val mucr = correctUcr_2
  private val ucr = correctUcr

  override def afterEach(): Unit = {
    reset(audit, connector, repository, movementBuilder)
    super.afterEach()
  }

  "Submit Associate" should {

    "delegate to connector" when {
      "Associate DUCR" in {
        when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.successful(conversationId))
        when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))

        val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(UcrType.Ducr, ucr)))
        await(service.submit(providerId, answers))

        theAssociationSubmitted[AssociateDUCRExchange] mustBe AssociateDUCRExchange(providerId, validEori, mucr, ucr)
        verify(repository).removeByProviderId(providerId)
        verify(audit).auditAssociate(providerId, mucr, ucr, "Success", Some(conversationId))
      }

      "Associate MUCR" in {
        when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.successful(conversationId))
        when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))

        val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(UcrType.Mucr, ucr)))
        await(service.submit(providerId, answers))

        theAssociationSubmitted[AssociateMUCRExchange] mustBe AssociateMUCRExchange(providerId, validEori, mucr, ucr)
        verify(repository).removeByProviderId(providerId)
        verify(audit).auditAssociate(providerId, mucr, ucr, "Success", Some(conversationId))
      }
    }

    "audit when failed" in {
      when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))

      val answers = AssociateUcrAnswers(Some(eori), Some(MucrOptions(mucr)), Some(AssociateUcr(UcrType.Ducr, ucr)))
      intercept[RuntimeException] {
        await(service.submit(providerId, answers))
      }

      theAssociationSubmitted[AssociateDUCRExchange] mustBe AssociateDUCRExchange(providerId, validEori, mucr, ucr)
      verify(repository, times(0)).removeByProviderId(providerId)
      verify(audit).auditAssociate(providerId, mucr, ucr, "Failed")
    }

    "handle missing eori" in {
      val answers = AssociateUcrAnswers(None, Some(MucrOptions(mucr)), Some(AssociateUcr(UcrType.Ducr, ucr)))
      intercept[Throwable] {
        await(service.submit(providerId, answers))
      } mustBe ReturnToStartException

      verifyNoInteractions(repository)
      verifyNoInteractions(audit)
    }

    "handle missing ucr" in {
      val answers = AssociateUcrAnswers(Some(eori), None, None)
      intercept[Throwable] {
        await(service.submit(providerId, answers))
      } mustBe ReturnToStartException

      verifyNoInteractions(repository)
      verifyNoInteractions(audit)
    }

    def theAssociationSubmitted[T <: ConsolidationExchange]: T = {
      val captor: ArgumentCaptor[ConsolidationExchange] = ArgumentCaptor.forClass(classOf[ConsolidationExchange])
      verify(connector).submit(captor.capture())(any())
      captor.getValue.asInstanceOf[T]
    }
  }

  "Submit Disassociate" should {

    "delegate to connector" when {

      "Disassociate MUCR" in {
        when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.successful(conversationId))
        when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(validEori), Some(DisassociateUcr(UcrType.Mucr, None, Some(mucr))))
        await(service.submit(providerId, answers))

        theDisassociationSubmitted[DisassociateMUCRExchange] mustBe DisassociateMUCRExchange(providerId, validEori, mucr)
        verify(repository).removeByProviderId(providerId)
        verify(audit).auditDisassociate(providerId, mucr, "Success", Some(conversationId))
      }

      "Disassociate DUCR" in {
        when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.successful(conversationId))
        when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(validEori), Some(DisassociateUcr(UcrType.Ducr, Some(ucr), None)))
        await(service.submit(providerId, answers))

        theDisassociationSubmitted[DisassociateDUCRExchange] mustBe DisassociateDUCRExchange(providerId, validEori, ucr)
        verify(repository).removeByProviderId(providerId)
        verify(audit).auditDisassociate(providerId, ucr, "Success", Some(conversationId))
      }
    }

    "audit when failed" in {
      when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))

      val answers = DisassociateUcrAnswers(Some(validEori), Some(DisassociateUcr(UcrType.Mucr, None, Some(mucr))))
      intercept[RuntimeException] {
        await(service.submit(providerId, answers))
      }

      theDisassociationSubmitted[DisassociateMUCRExchange] mustBe DisassociateMUCRExchange(providerId, validEori, mucr)
      verify(repository, times(0)).removeByProviderId(providerId)
      verify(audit).auditDisassociate(providerId, mucr, "Failed")
    }

    "handle missing eori" in {
      val answers = DisassociateUcrAnswers(None, Some(DisassociateUcr(UcrType.Mucr, None, Some(mucr))))
      intercept[Throwable] {
        await(service.submit(providerId, answers))
      } mustBe ReturnToStartException

      verifyNoInteractions(repository)
      verifyNoInteractions(audit)
    }

    "handle missing ucr" when {

      "block is empty" in {
        val answers = DisassociateUcrAnswers(Some(validEori), None)
        intercept[Throwable] {
          await(service.submit(providerId, answers))
        } mustBe ReturnToStartException

        verifyNoInteractions(repository)
        verifyNoInteractions(audit)
      }

      "missing fields" when {

        "Disassociate MUCR" in {
          val answers = DisassociateUcrAnswers(Some(validEori), Some(DisassociateUcr(UcrType.Mucr, None, None)))
          intercept[Throwable] {
            await(service.submit(providerId, answers))
          } mustBe ReturnToStartException

          verifyNoInteractions(repository)
          verifyNoInteractions(audit)
        }

        "Disassociate DUCR" in {
          val answers = DisassociateUcrAnswers(Some(validEori), Some(DisassociateUcr(UcrType.Ducr, None, None)))
          intercept[Throwable] {
            await(service.submit(providerId, answers))
          } mustBe ReturnToStartException

          verifyNoInteractions(repository)
          verifyNoInteractions(audit)
        }
      }
    }

    def theDisassociationSubmitted[T <: ConsolidationExchange]: T = {
      val captor: ArgumentCaptor[ConsolidationExchange] = ArgumentCaptor.forClass(classOf[ConsolidationExchange])
      verify(connector).submit(captor.capture())(any())
      captor.getValue.asInstanceOf[T]
    }
  }

  "Submit ShutMUCR" should {

    "delegate to connector" in {
      when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.successful(conversationId))
      when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))

      val answers = ShutMucrAnswers(Some(validEori), Some(ShutMucr(mucr)))
      await(service.submit(providerId, answers))

      theShutMucrSubmitted mustBe ShutMUCRExchange(providerId, validEori, mucr)
      verify(repository).removeByProviderId(providerId)
      verify(audit).auditShutMucr(providerId, mucr, "Success", Some(conversationId))
    }

    "audit when failed" in {
      when(connector.submit(any[ConsolidationExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))

      val answers = ShutMucrAnswers(Some(validEori), Some(ShutMucr(mucr)))
      intercept[RuntimeException] {
        await(service.submit(providerId, answers))
      }

      theShutMucrSubmitted mustBe ShutMUCRExchange(providerId, validEori, mucr)
      verify(repository, times(0)).removeByProviderId(providerId)
      verify(audit).auditShutMucr(providerId, mucr, "Failed")
    }

    "handle missing eori" in {
      val answers = ShutMucrAnswers(None, Some(ShutMucr(mucr)))
      intercept[Throwable] {
        await(service.submit(providerId, answers))
      } mustBe ReturnToStartException

      verifyNoInteractions(repository)
      verifyNoInteractions(audit)
    }

    "handle missing mucr" in {
      val answers = ShutMucrAnswers(Some(validEori), None)
      intercept[Throwable] {
        await(service.submit(providerId, answers))
      } mustBe ReturnToStartException

      verifyNoInteractions(repository)
      verifyNoInteractions(audit)
    }

    def theShutMucrSubmitted: ShutMUCRExchange = {
      val captor: ArgumentCaptor[ShutMUCRExchange] = ArgumentCaptor.forClass(classOf[ShutMUCRExchange])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Movement" when {

    "provided with Arrival" when {

      "everything works correctly" should {

        "call MovementBuilder, AuditService, backend Connector, CacheRepository and AuditService again" in {
          when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.successful(conversationId))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val arrivalExchange = validArrivalExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(arrivalExchange)

          val answers = validArrivalAnswers

          await(service.submit(providerId, answers))

          val inOrder = Mockito.inOrder(movementBuilder, repository, audit, connector)
          inOrder.verify(movementBuilder).createMovementExchange(meq(providerId), meq(answers))
          inOrder.verify(audit).auditAllPagesUserInput(meq(providerId), meq(answers))(any())
          inOrder.verify(connector).submit(meq(arrivalExchange))(any())
          inOrder.verify(repository).removeByProviderId(meq(providerId))
          inOrder.verify(audit).auditMovements(meq(arrivalExchange), meq("Success"), meq(AuditType.AuditArrival), meq(Some(conversationId)))(any())
        }
      }

      "MovementBuilder throws ReturnToStartException" should {
        "propagate the exception" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          } mustBe ReturnToStartException
        }

        "not call AuditService, backend Connector and CacheRepository" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(audit)
          verifyNoInteractions(connector)
          verifyNoInteractions(repository)
        }
      }

      "backend Connector returns Failed Future" should {

        "not call CacheRepository" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(validArrivalExchange)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(repository)
        }

        "call AuditService second time with failed result" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val arrivalExchange = validArrivalExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(arrivalExchange)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verify(audit).auditMovements(meq(arrivalExchange), meq("Failed"), meq(AuditType.AuditArrival), any[Option[String]])(any())
        }
      }
    }

    "provided with Retrospective Arrival" when {

      "everything works correctly" should {

        "call MovementBuilder, AuditService, backend Connector, CacheRepository and AuditService again" in {
          when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.successful(conversationId))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val retroArrivalExchange = validRetrospectiveArrivalExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(retroArrivalExchange)

          val answers = validRetrospectiveArrivalAnswers

          await(service.submit(providerId, answers))

          val inOrder = Mockito.inOrder(movementBuilder, repository, audit, connector)
          inOrder.verify(movementBuilder).createMovementExchange(meq(providerId), meq(answers))
          inOrder.verify(audit).auditAllPagesUserInput(meq(providerId), meq(answers))(any())
          inOrder.verify(connector).submit(meq(retroArrivalExchange))(any())
          inOrder.verify(repository).removeByProviderId(meq(providerId))
          inOrder
            .verify(audit)
            .auditMovements(meq(retroArrivalExchange), meq("Success"), meq(AuditType.AuditRetrospectiveArrival), meq(Some(conversationId)))(any())
        }
      }

      "MovementBuilder throws ReturnToStartException" should {
        "propagate the exception" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validRetrospectiveArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          } mustBe ReturnToStartException
        }

        "not call AuditService, backend Connector and CacheRepository" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validRetrospectiveArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(audit)
          verifyNoInteractions(connector)
          verifyNoInteractions(repository)
        }
      }

      "backend Connector returns Failed Future" should {

        "not call CacheRepository" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(validRetrospectiveArrivalExchange)

          val answers = validRetrospectiveArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(repository)
        }

        "call AuditService second time with failed result" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val retroArrivalExchange = validRetrospectiveArrivalExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(retroArrivalExchange)

          val answers = validRetrospectiveArrivalAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verify(audit).auditMovements(meq(retroArrivalExchange), meq("Failed"), meq(AuditType.AuditRetrospectiveArrival), any[Option[String]])(any())
        }
      }
    }

    "provided with Departure" when {

      "everything works correctly" should {

        "call MovementBuilder, AuditService, backend Connector, CacheRepository and AuditService again" in {
          when(repository.removeByProviderId(anyString())).thenReturn(Future.successful((): Unit))
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.successful(conversationId))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val departureExchange = validDepartureExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(departureExchange)

          val answers = validDepartureAnswers

          await(service.submit(providerId, answers))

          val inOrder = Mockito.inOrder(movementBuilder, repository, audit, connector)
          inOrder.verify(movementBuilder).createMovementExchange(meq(providerId), meq(answers))
          inOrder.verify(audit).auditAllPagesUserInput(meq(providerId), meq(answers))(any())
          inOrder.verify(connector).submit(meq(departureExchange))(any())
          inOrder.verify(repository).removeByProviderId(meq(providerId))
          inOrder
            .verify(audit)
            .auditMovements(meq(departureExchange), meq("Success"), meq(AuditType.AuditDeparture), meq(Some(conversationId)))(any())
        }
      }

      "MovementBuilder throws ReturnToStartException" should {
        "propagate the exception" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          } mustBe ReturnToStartException
        }

        "not call AuditService, backend Connector and CacheRepository" in {
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenThrow(ReturnToStartException)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(audit)
          verifyNoInteractions(connector)
          verifyNoInteractions(repository)
        }
      }

      "backend Connector returns Failed Future" should {

        "not call CacheRepository" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(validDepartureExchange)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verifyNoInteractions(repository)
        }

        "call AuditService second time with failed result" in {
          when(connector.submit(any[MovementExchange]())(any())).thenReturn(Future.failed(new RuntimeException("Error")))
          when(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).thenReturn(Future.successful(AuditResult.Success))
          when(audit.auditMovements(any[MovementExchange], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .thenReturn(Future.successful(AuditResult.Success))
          val departureExchange = validDepartureExchange
          when(movementBuilder.createMovementExchange(anyString(), any[MovementAnswers])).thenReturn(departureExchange)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(providerId, answers))
          }

          verify(audit).auditMovements(meq(departureExchange), meq("Failed"), meq(AuditType.AuditDeparture), any[Option[String]])(any())
        }
      }
    }
  }
}
