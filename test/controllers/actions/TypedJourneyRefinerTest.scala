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

package controllers.actions

import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import controllers.routes.ChoiceController
import models.cache.{ArrivalAnswers, Cache, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.CacheRepository
import testdata.CommonTestData.providerId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TypedJourneyRefinerTest extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val repository = mock[CacheRepository]
  private val block = mock[JourneyRequest[?] => Future[Result]]
  private val operator = Operator(providerId)
  private val request = AuthenticatedRequest(operator, FakeRequest())
  private val answers = ArrivalAnswers()
  private val cache = Cache(providerId, Some(answers), None)

  private val refiner = new JourneyRefiner(repository)

  override def afterEach(): Unit = {
    reset(repository, block)
    super.afterEach()
  }

  "refine" should {
    "permit request" when {
      "answers found" when {
        "on unshared journey" in {
          when(block.apply(any())).thenReturn(Future.successful(Results.Ok))
          when(repository.findByProviderId(providerId)).thenReturn(Future.successful(Some(cache)))

          await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cache, request)
        }

        "on shared journey" in {
          when(block.apply(any())).thenReturn(Future.successful(Results.Ok))
          when(repository.findByProviderId(providerId)).thenReturn(Future.successful(Some(cache)))

          await(refiner(JourneyType.DEPART, JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cache, request)
        }
      }

      def theRequestBuilt: JourneyRequest[?] = {
        val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[?]])
        verify(block).apply(captor.capture())
        captor.getValue
      }
    }

    "block request" when {
      "answers not found" in {
        when(repository.findByProviderId(providerId)).thenReturn(Future.successful(None))

        await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Redirect(ChoiceController.displayPage)
      }

      "answers found of a different type" in {
        when(repository.findByProviderId(providerId)).thenReturn(Future.successful(None))

        await(refiner(JourneyType.DEPART).invokeBlock(request, block)) mustBe Results.Redirect(ChoiceController.displayPage)
      }
    }
  }
}
