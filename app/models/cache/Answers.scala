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

import models.cache.JourneyType.JourneyType
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.json.Union
import forms.{AssociateUcr, MucrOptions}

case class ArrivalAnswers(override val eori: Option[String] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ARRIVE
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]
}

case class DepartureAnswers(override val eori: Option[String] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DEPART
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]
}

case class AssociateUcrAnswers(
  override val eori: Option[String] = None,
  mucrOptions: Option[MucrOptions] = None,
  associateUcr: Option[AssociateUcr] = None
) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ASSOCIATE_UCR
}

object AssociateUcrAnswers {
  implicit val format: Format[AssociateUcrAnswers] = Json.format[AssociateUcrAnswers]
}

case class DisassociateUcrAnswers(override val eori: Option[String] = Answers.fakeEORI, ucr: Option[String] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object DisassociateUcrAnswers {
  implicit val format: Format[DisassociateUcrAnswers] = Json.format[DisassociateUcrAnswers]
}

case class ShutMucrAnswers(override val eori: Option[String] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object ShutMucrAnswers {
  implicit val format: Format[ShutMucrAnswers] = Json.format[ShutMucrAnswers]
}

trait Answers {
  val `type`: JourneyType
  val eori: Option[String]
}

object Answers {
  implicit val format: Format[Answers] = Union
    .from[Answers]("type")
    .and[ArrivalAnswers](JourneyType.ARRIVE.toString)
    .and[DepartureAnswers](JourneyType.DEPART.toString)
    .and[AssociateUcrAnswers](JourneyType.ASSOCIATE_UCR.toString)
    .and[DisassociateUcrAnswers](JourneyType.DISSOCIATE_UCR.toString)
    .and[ShutMucrAnswers](JourneyType.SHUT_MUCR.toString)
    .format

  val fakeEORI = Some("GB1234567890")
}
