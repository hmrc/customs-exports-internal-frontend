package connectors

import play.api.Configuration

trait MovementsBackendWiremockTestServer extends WiremockTestServer {

  protected val movementsBackendConfiguration: Configuration =
    Configuration.from(Map("microservice.services.customs-declare-exports-movements.port" -> wirePort))

}
