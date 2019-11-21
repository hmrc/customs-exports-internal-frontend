import connectors.{AuthWiremockTestServer, MovementsBackendWiremockTestServer}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Call
import play.api.test.Helpers._
import play.api.{Application, Logger}
import reactivemongo.play.json.collection.JSONCollection
import repositories.CacheRepository

import scala.concurrent.ExecutionContext.Implicits.global

class IntegrationSpec
    extends WordSpec with MustMatchers with BeforeAndAfterEach with GuiceOneServerPerSuite with AuthWiremockTestServer
    with MovementsBackendWiremockTestServer {

  override lazy val port = 14681
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(Map(
        "play.filters.disabled" -> "play.filters.csrf.CSRFFilter",
        authConfiguration,
        movementsBackendConfiguration
      ))
      .build()

  private val logger = Logger(this.getClass)
  private lazy val serviceUrl = "http://localhost:" + port
  private lazy val wsClient = app.injector.instanceOf[WSClient]
  protected lazy val cache: JSONCollection = app.injector.instanceOf[CacheRepository].collection

  override protected def afterAll(): Unit = {
    await(cache.drop(failIfNotFound = false))
    super.afterAll()
  }

  protected def get(call: Call): WSResponse = {
    val url = serviceUrl + call.url
    logger.info(s"GET-ing [$url]")
    await(wsClient.url(url).get())
  }

  protected def post[T](call: Call, payload: T)(implicit wts: Writes[T]): WSResponse = {
    val url = serviceUrl + call.url
    logger.info(s"POST-ing to [$url]")
    await(wsClient.url(url).post(Json.toJson(payload)))
  }

}
