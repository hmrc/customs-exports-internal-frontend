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
  val ASSOCIATE_DUCR, DISASSOCIATE_DUCR, SHUT_MUCR = Value
  implicit val format: Format[ConsolidationType] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

case class AssociateUCRExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_DUCR
}

object AssociateUCRExchange {
  implicit val format: OFormat[AssociateUCRExchange] = Json.format[AssociateUCRExchange]
}

case class DisassociateDUCRExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_DUCR
}

object DisassociateDUCRExchange {
  implicit val format: OFormat[DisassociateDUCRExchange] = Json.format[DisassociateDUCRExchange]
}

case class ShutMUCRExchange(override val providerId: String, override val eori: String, mucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.SHUT_MUCR
}
object ShutMUCRExchange {
  implicit val format: OFormat[ShutMUCRExchange] = Json.format[ShutMUCRExchange]
}

trait ConsolidationExchange {
  val consolidationType: ConsolidationType
  val eori: String
  val providerId: String
}

object ConsolidationExchange {
  implicit val format: Format[ConsolidationExchange] = Union
    .from[ConsolidationExchange](typeField = "consolidationType")
    .and[AssociateUCRExchange](typeTag = ConsolidationType.ASSOCIATE_DUCR.toString)
    .and[DisassociateDUCRExchange](typeTag = ConsolidationType.DISASSOCIATE_DUCR.toString)
    .and[ShutMUCRExchange](typeTag = ConsolidationType.SHUT_MUCR.toString)
    .format
}
