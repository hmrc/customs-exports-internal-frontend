import connectors.{AuthWiremockTestServer, MovementsBackendWiremockTestServer}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}
import play.api.{Application, Logger}
import reactivemongo.play.json.collection.JSONCollection
import repositories.CacheRepository
import repository.TestMongoDB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IntegrationSpec
    extends WordSpec with MustMatchers with BeforeAndAfterEach with GuiceOneServerPerSuite with AuthWiremockTestServer
    with MovementsBackendWiremockTestServer with TestMongoDB {

  /*
    Intentionally NOT exposing the real CacheRepository as we shouldn't test our production code using our production classes.
   */
  protected lazy val cache: JSONCollection = app.injector.instanceOf[CacheRepository].collection

  override lazy val port = 14681
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(authConfiguration)
      .configure(movementsBackendConfiguration)
      .configure(mongoConfiguration)
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(cache.drop(failIfNotFound = false))
  }

  protected def get(call: Call): Future[Result] =
    route(app, FakeRequest("GET", call.url)).get

  protected def post[T](call: Call, payload: (String, String)*): Future[Result] = {
    val request: Request[AnyContentAsFormUrlEncoded] = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", call.url).withFormUrlEncodedBody(payload: _*))
    route(app, request).get
  }

}
