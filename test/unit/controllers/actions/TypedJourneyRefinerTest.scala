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

package controllers.actions

import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import models.cache.{ArrivalAnswers, Cache, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.CacheRepository
import testdata.CommonTestData.providerId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TypedJourneyRefinerTest extends WordSpec with MustMatchers with MockitoSugar with BeforeAndAfterEach {

  private val repository = mock[CacheRepository]
  private val block = mock[JourneyRequest[_] => Future[Result]]
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
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(repository.findByProviderId(providerId)).willReturn(Future.successful(Some(cache)))

          await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cache, request)
        }

        "on shared journey" in {
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(repository.findByProviderId(providerId)).willReturn(Future.successful(Some(cache)))

          await(refiner(JourneyType.DEPART, JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cache, request)
        }
      }

      def theRequestBuilt: JourneyRequest[_] = {
        val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[_]])
        verify(block).apply(captor.capture())
        captor.getValue
      }
    }

    "block request" when {
      "answers not found" in {
        given(repository.findByProviderId(providerId)).willReturn(Future.successful(None))

        await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayPage())
      }

      "answers found of a different type" in {
        given(repository.findByProviderId(providerId)).willReturn(Future.successful(None))

        await(refiner(JourneyType.DEPART).invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayPage())
      }
    }
  }

}
