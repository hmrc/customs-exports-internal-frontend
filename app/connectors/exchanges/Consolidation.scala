package connectors.exchanges

import connectors.exchanges
import connectors.exchanges.ConsolidationType.ConsolidationType
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union


object ConsolidationType extends Enumeration {
  type ConsolidationType = Value
  val ASSOCIATE_DUCR, DISSOCIATE_DUCR, SHUT_MUCR = Value
  implicit val format: Format[ConsolidationType] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

case class DisassociateDUCRRequest(override val providerId: String,
                                   override val eori: String,
                                   ucr: String) extends Consolidation {
  override val `type`: exchanges.ConsolidationType.Value = ConsolidationType.DISSOCIATE_DUCR
}
object DisassociateDUCRRequest {
  implicit val format: OFormat[DisassociateDUCRRequest] = Json.format[DisassociateDUCRRequest]
}

trait Consolidation {
  val `type`: ConsolidationType
  val eori: String
  val providerId: String
}

object Consolidation {
  implicit val format: Format[Consolidation] = Union.from[Consolidation](typeField = "type")
    .and[DisassociateDUCRRequest](typeTag = ConsolidationType.ASSOCIATE_DUCR.toString)
    .format
}
