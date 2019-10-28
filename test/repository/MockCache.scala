package repository

import models.cache.Cache
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import repositories.MovementRepository

import scala.concurrent.Future

trait MockCache extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  protected val cache: MovementRepository = mock[MovementRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(cache.upsert(any())).willAnswer(withTheCacheUpserted)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(cache)
    super.afterEach()
  }

  protected def givenTheCacheContains(content: Cache): Unit =
    given(cache.findByPid(any())).willReturn(Future.successful(Some(content)))

  protected def givenTheCacheIsEmpty(): Unit =
    given(cache.findByPid(any())).willReturn(Future.successful(None))

  protected def theCacheUpserted: Cache = {
    val captor: ArgumentCaptor[Cache] = ArgumentCaptor.forClass(classOf[Cache])
    verify(cache).upsert(captor.capture())
    captor.getValue
  }

  protected def withTheCacheUpserted: Answer[Future[Cache]] = new Answer[Future[Cache]] {
    override def answer(invocation: InvocationOnMock): Future[Cache] = Future.successful(invocation.getArgument(0))
  }

}
