package models.cache

import models.cache
import models.cache.JourneyType.JourneyType
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.json.Union

case class Arrival(field1: Option[String]) extends Answers {
  override val `type`: cache.JourneyType.Value = JourneyType.ARRIVE

}

object Arrival {
  implicit val format: Format[Arrival] = Json.format[Arrival]
}

case class Departure(someFields: String) extends Answers {
  override val `type`: cache.JourneyType.Value = JourneyType.DEPART
}

object Departure {
  implicit val format: Format[Departure] = Json.format[Departure]
}

trait Answers {
  val `type`: JourneyType
}
object Answers {
  implicit val format: Format[Answers] = Union.from[Answers]("type")
    .and[Arrival](JourneyType.ARRIVE.toString)
    .and[Departure](JourneyType.DEPART.toString)
    .format
}
