package models.cache

import play.api.libs.json.{Format, Reads, Writes}

object JourneyType extends Enumeration {
  type JourneyType = Value

  val ARRIVE, DEPART = Value

  implicit val format: Format[JourneyType] = Format(Reads.enumNameReads(JourneyType), Writes.enumNameWrites)
}
