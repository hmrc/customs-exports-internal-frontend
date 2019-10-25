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

case class ArrivalAnswers(field1: Option[String]) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ARRIVE
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]
}

case class DepartureAnswers(field1: Option[String]) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DEPART
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]
}

case class AssociateUcr(field1: Option[String]) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ASSOCIATE_UCR
}

object AssociateUcr {
  implicit val format: Format[AssociateUcr] = Json.format[AssociateUcr]
}

case class DissociateUcr(field1: Option[String]) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object DissociateUcr {
  implicit val format: Format[DissociateUcr] = Json.format[DissociateUcr]
}

case class ShutMucr(field1: Option[String]) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object ShutMucr {
  implicit val format: Format[ShutMucr] = Json.format[ShutMucr]
}

trait Answers {
  val `type`: JourneyType
}
object Answers {
  implicit val format: Format[Answers] = Union.from[Answers]("type")
    .and[ArrivalAnswers](JourneyType.ARRIVE.toString)
    .and[DepartureAnswers](JourneyType.DEPART.toString)
    .and[AssociateUcr](JourneyType.ASSOCIATE_UCR.toString)
    .and[DissociateUcr](JourneyType.DISSOCIATE_UCR.toString)
    .and[ShutMucr](JourneyType.SHUT_MUCR.toString)
    .format
}
