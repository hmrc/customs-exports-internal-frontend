package connectors

trait MovementsBackendWiremockTestServer extends WiremockTestServer {

  protected val movementsBackendConfiguration: (String, Any) = "microservice.services.customs-declare-exports-movements.port" -> wirePort

}
