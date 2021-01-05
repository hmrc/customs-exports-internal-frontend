/*
 * Copyright 2021 HM Revenue & Customs
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

import models.cache.Cache
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import repositories.CacheRepository

import scala.concurrent.Future

trait MockCache extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  protected val cacheRepository: CacheRepository = mock[CacheRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(cacheRepository.upsert(any())).willAnswer(withTheCacheUpserted)
    given(cacheRepository.removeByProviderId(any())).willReturn(Future.successful((): Unit))
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(cacheRepository)
    super.afterEach()
  }

  protected def givenTheCacheContains(content: Cache): Unit =
    given(cacheRepository.findByProviderId(any())).willReturn(Future.successful(Some(content)))

  protected def givenTheCacheIsEmpty(): Unit =
    given(cacheRepository.findByProviderId(any())).willReturn(Future.successful(None))

  protected def theCacheUpserted: Cache = {
    val captor: ArgumentCaptor[Cache] = ArgumentCaptor.forClass(classOf[Cache])
    verify(cacheRepository).upsert(captor.capture())
    captor.getValue
  }

  protected def successfulRemoving(): Unit =
    given(cacheRepository.removeByProviderId(any())).willReturn(Future.successful((): Unit))

  protected def withTheCacheUpserted: Answer[Future[Cache]] = new Answer[Future[Cache]] {
    override def answer(invocation: InvocationOnMock): Future[Cache] = Future.successful(invocation.getArgument(0))
  }
}
