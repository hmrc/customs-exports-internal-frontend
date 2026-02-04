/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.exchanges.ActionType.ConsolidationType
import play.api.libs.json._
import utils.Union

sealed trait ConsolidationExchange {
  val consolidationType: ConsolidationType
  val eori: String
  val providerId: String
}

object ConsolidationExchange {
  implicit val associateDucrFormat: OFormat[AssociateDUCRExchange] = Json.format[AssociateDUCRExchange]
  implicit val associateDucrPartFormat: OFormat[AssociateDUCRPartExchange] = Json.format[AssociateDUCRPartExchange]
  implicit val associateMucrFormat: OFormat[AssociateMUCRExchange] = Json.format[AssociateMUCRExchange]
  implicit val disassociateDucrFormat: OFormat[DisassociateDUCRExchange] = Json.format[DisassociateDUCRExchange]
  implicit val disassociateDucrPartFormat: OFormat[DisassociateDUCRPartExchange] = Json.format[DisassociateDUCRPartExchange]
  implicit val disassociateMucrFormat: OFormat[DisassociateMUCRExchange] = Json.format[DisassociateMUCRExchange]
  implicit val shutMucrFormat: OFormat[ShutMUCRExchange] = Json.format[ShutMUCRExchange]

  implicit val format: Format[ConsolidationExchange] = Union
    .from[ConsolidationExchange](typeField = "consolidationType")
    .and[AssociateDUCRExchange](typeTag = ConsolidationType.DucrAssociation.typeName)
    .and[AssociateDUCRPartExchange](typeTag = ConsolidationType.DucrPartAssociation.typeName)
    .and[AssociateMUCRExchange](typeTag = ConsolidationType.MucrAssociation.typeName)
    .and[DisassociateDUCRExchange](typeTag = ConsolidationType.DucrDisassociation.typeName)
    .and[DisassociateDUCRPartExchange](typeTag = ConsolidationType.DucrPartDisassociation.typeName)
    .and[DisassociateMUCRExchange](typeTag = ConsolidationType.MucrDisassociation.typeName)
    .and[ShutMUCRExchange](typeTag = ConsolidationType.ShutMucr.typeName)
    .format
}

case class AssociateDUCRExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String)
    extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrAssociation
}

case class AssociateDUCRPartExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String)
    extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrPartAssociation
}

case class AssociateMUCRExchange(override val providerId: String, override val eori: String, mucr: String, ucr: String)
    extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrAssociation
}

case class DisassociateDUCRExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrDisassociation
}

case class DisassociateDUCRPartExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrPartDisassociation
}

case class DisassociateMUCRExchange(override val providerId: String, override val eori: String, ucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrDisassociation
}

case class ShutMUCRExchange(override val providerId: String, override val eori: String, mucr: String) extends ConsolidationExchange {
  override val consolidationType: ConsolidationType = ConsolidationType.ShutMucr
}
