/*
 * Copyright 2023 HM Revenue & Customs
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

package models.submissions

import connectors.exchanges.ActionType
import models.{now, UcrBlock}
import models.UcrType.{Ducr, DucrPart, Mucr}
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.UUID

case class Submission(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = now
) {

  def hasMucr: Boolean = ucrBlocks.exists(_ is Mucr)

  def hasDucr: Boolean = ucrBlocks.exists(_ is Ducr)

  def hasDucrPart: Boolean = ucrBlocks.exists(_ is DucrPart)

  lazy val extractUcr: Option[(String, String)] =
    ucrBlocks
      .find(_ is Mucr) // Mucr has priority over Ducr and Ducr Part
      .fold(ucrBlocks.headOption.flatMap(_.typeAndValue))(_.typeAndValue)
}

object Submission {
  implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[Submission] = Json.format[Submission]
}
