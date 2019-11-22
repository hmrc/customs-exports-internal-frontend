package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import play.api.Configuration
import play.api.http.Status

trait MovementsBackendWiremockTestServer extends WiremockTestServer {

  protected val movementsBackendConfiguration: Configuration =
    Configuration.from(Map("microservice.services.customs-declare-exports-movements.port" -> wirePort))

  protected def givenMovementsBackendAcceptsTheConsolidation(): Unit = {
    stubFor(
      post("/consolidation")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )
  }

  protected def givenTheMovementsBackendAcceptsTheMovement(): Unit = {
    stubFor(
      post("/movements")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )
  }

  protected def postRequestedForConsolidation(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/consolidation"))
  protected def postRequestedForMovement(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/movements"))

}
