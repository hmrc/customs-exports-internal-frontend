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

package models.notifications

import models.now
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class NotificationFrontendModel(
  timestampReceived: Instant = now,
  conversationId: String,
  responseType: ResponseType,
  entries: Seq[Entry],
  crcCode: Option[String],
  actionCode: Option[String],
  errorCodes: Seq[String],
  messageCode: String
) extends Ordered[NotificationFrontendModel] {

  override def compare(other: NotificationFrontendModel): Int =
    this.timestampReceived.compareTo(other.timestampReceived)
}

object NotificationFrontendModel {
  implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[NotificationFrontendModel] = Json.format[NotificationFrontendModel]
}
