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

package models.cache

import models.{now, UcrBlock}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class Cache(providerId: String, answers: Option[Answers], queryUcr: Option[UcrBlock], updated: Option[Instant] = Some(now)) {
  def update(answers: Answers): Cache = this.copy(answers = Some(answers), updated = Some(now))
}

object Cache {
  private val stringDateReads: Reads[Instant] = implicitly[Reads[String]].map(Instant.parse)

  private val mongoDateReads: Reads[Instant] = MongoJavatimeFormats.instantReads

  private val instantReads: Reads[Instant] = mongoDateReads orElse stringDateReads

  implicit val formatWrites: Writes[Instant] = MongoJavatimeFormats.instantWrites

  implicit val reads: Reads[Cache] = (
    (JsPath \ "providerId").read[String] and
      (JsPath \ "answers").readNullable[Answers] and
      (JsPath \ "queryUcr").readNullable[UcrBlock] and
      (JsPath \ "updated").readNullable[Instant](instantReads)
  )(new Cache(_, _, _, _))

  implicit val writes: OWrites[Cache] = Json.writes[Cache]

  implicit val format: OFormat[Cache] = OFormat(reads, writes)

  def apply(providerId: String, queryUcr: UcrBlock): Cache = new Cache(providerId, None, Some(queryUcr))
}
