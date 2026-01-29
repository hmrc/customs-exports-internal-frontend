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

package models

import models.UcrType
import play.api.libs.json.{Json, OFormat}

case class UcrBlock(ucr: String, ucrPartNo: Option[String] = None, ucrType: String, chiefUcr: Option[Boolean] = None) {

  def is(ucrType: UcrType): Boolean = this.ucrType.equals(ucrType.codeValue)

  def isNot(ucrType: UcrType): Boolean = !is(ucrType)

  val isChief: Boolean = chiefUcr.fold(false)(_.self)

  def fullUcr: String = ucr + ucrPartNo.map(ucrPartNoValue => s"-$ucrPartNoValue").getOrElse("")

  def typeAndValue: Option[(String, String)] = {
    val result: (String, String) = (if (is(UcrType.Ducr)) "DUCR" else if (is(UcrType.DucrPart)) "DUCR Part" else "MUCR", fullUcr)
    Some(result)
  }
}

object UcrBlock {
  implicit val format: OFormat[UcrBlock] = Json.format[UcrBlock]

  def apply(ucr: String, ucrType: UcrType): UcrBlock = UcrBlock(ucr = ucr, ucrType = ucrType.codeValue)
}
