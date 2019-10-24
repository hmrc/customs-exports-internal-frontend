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
