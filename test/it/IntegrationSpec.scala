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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import connectors.{AuditWiremockTestServer, AuthWiremockTestServer, MovementsBackendWiremockTestServer}
import models.cache.{Answers, Cache}
import models.{DateTimeProvider, UcrBlock}
import modules.DateTimeModule
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}
import repositories.CacheRepository
import repository.TestMongoDB

import java.time.{Clock, LocalDateTime}
import scala.concurrent.Future

abstract class IntegrationSpec
    extends AnyWordSpec with Matchers with BeforeAndAfterEach with GuiceOneServerPerSuite with AuthWiremockTestServer
    with MovementsBackendWiremockTestServer with AuditWiremockTestServer with Eventually with TestMongoDB {

  /*
    Intentionally NOT exposing the real CacheRepository as we shouldn't test our production code using our production classes.
   */
  private lazy val cacheRepository = app.injector.instanceOf[CacheRepository]

  val dateTimeProvider = new DateTimeProvider(Clock.fixed(LocalDateTime.now().atZone(DateTimeModule.timezone).toInstant, DateTimeModule.timezone))

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(authConfiguration)
      .configure(movementsBackendConfiguration)
      .configure(mongoConfiguration)
      .configure(auditConfiguration)
      .overrides(bind[DateTimeProvider].toInstance(dateTimeProvider))
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(cacheRepository.removeAll)
  }

  protected def get(call: Call): Future[Result] =
    route(app, FakeRequest("GET", call.url).withSession("authToken" -> "Token some-token")).get

  protected def post[T](call: Call, payload: (String, String)*): Future[Result] = {
    val request: Request[AnyContentAsFormUrlEncoded] =
      CSRFTokenHelper.addCSRFToken(FakeRequest("POST", call.url).withSession("authToken" -> "Token some-token").withFormUrlEncodedBody(payload: _*))
    route(app, request).get
  }

  protected def theCacheFor(pid: String): Option[Cache] =
    await(cacheRepository.findOne("providerId", pid))

  protected def theAnswersFor(pid: String): Option[Answers] = theCacheFor(pid).flatMap(_.answers)

  protected def givenCacheFor(pid: String, answers: Answers): Unit =
    cacheRepository.insertOne(Cache(providerId = pid, answers = Some(answers), queryUcr = None))

  protected def givenCacheFor(pid: String, queryUcr: UcrBlock): Unit = givenCacheFor(Cache(pid, queryUcr = queryUcr))
  protected def givenCacheFor(cache: Cache): Unit = await(cacheRepository.insertOne(cache))

  protected def verifyEventually(requestPatternBuilder: RequestPatternBuilder): Unit = eventually(WireMock.verify(requestPatternBuilder))
}
