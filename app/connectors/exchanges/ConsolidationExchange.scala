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

package connectors.exchanges

import connectors.exchanges.ConsolidationType.ConsolidationType
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union

object ConsolidationType extends Enumeration {
  type ConsolidationType = Value

  val ASSOCIATE_DUCR, ASSOCIATE_MUCR, DISASSOCIATE_DUCR, DISASSOCIATE_MUCR, SHUT_MUCR = Value

  implicit val format: Format[ConsolidationType] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

case class AssociateDUCRExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String)
    extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_DUCR
}
object AssociateDUCRExchange {
  implicit val format: OFormat[AssociateDUCRExchange] = Json.format[AssociateDUCRExchange]
}

case class AssociateMUCRExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String)
    extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_MUCR
}
object AssociateMUCRExchange {
  implicit val format: OFormat[AssociateMUCRExchange] = Json.format[AssociateMUCRExchange]
}

case class DisassociateDUCRExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_DUCR
}
object DisassociateDUCRExchange {
  implicit val format: OFormat[DisassociateDUCRExchange] = Json.format[DisassociateDUCRExchange]
}

case class DisassociateMUCRExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_DUCR
}
object DisassociateMUCRExchange {
  implicit val format: OFormat[DisassociateMUCRExchange] = Json.format[DisassociateMUCRExchange]
}

case class ShutMUCRExchange(override val providerId: String, override val eori: String, mucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.SHUT_MUCR
}
object ShutMUCRExchange {
  implicit val format: OFormat[ShutMUCRExchange] = Json.format[ShutMUCRExchange]
}

sealed trait ConsolidationExchange {
  val consolidationType: ConsolidationType
  val eori: String
  val providerId: String
}

object ConsolidationExchange {
  implicit val format: Format[ConsolidationExchange] = Union
    .from[ConsolidationExchange](typeField = "consolidationType")
    .and[AssociateDUCRExchange](typeTag = ConsolidationType.ASSOCIATE_DUCR.toString)
    .and[AssociateMUCRExchange](typeTag = ConsolidationType.ASSOCIATE_MUCR.toString)
    .and[DisassociateDUCRExchange](typeTag = ConsolidationType.DISASSOCIATE_DUCR.toString)
    .and[DisassociateMUCRExchange](typeTag = ConsolidationType.DISASSOCIATE_MUCR.toString)
    .and[ShutMUCRExchange](typeTag = ConsolidationType.SHUT_MUCR.toString)
    .format
}
