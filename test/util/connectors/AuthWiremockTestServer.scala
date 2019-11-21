package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import play.api.libs.json.Json
import play.api.test.Helpers.{OK, UNAUTHORIZED}

trait AuthWiremockTestServer extends WiremockTestServer {

  private val mandatoryRoles: Seq[String] = Seq("write:inventory-linking-exports")
  protected val authConfiguration: (String, Any) = "microservice.services.auth.port" -> wirePort

  protected def givenAuthSuccess(pid: String = "1", role: Seq[String] = mandatoryRoles): Unit = {
    val response = Json.obj(
      "optionalCredentials" -> Json.obj(
        "providerId" -> pid,
        "providerType" -> "PrivilegedApplication"
      ),
      "allEnrolments" -> role.map(value => Json.obj(
        "key" -> value,
        "identifiers" -> Json.arr(),
        "state" -> "state"
      ))
    )

    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(response.toString())
      )
    )
  }

  protected def givenAuthFailed(): Unit = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(UNAUTHORIZED)
      )
    )
  }

}
