/*
 * Copyright 2020 HM Revenue & Customs
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

package views
import play.api.i18n.Messages

object IleCodeMapper {

  val definedIcsCodes: Set[String] = Set("3", "6")
  val definedRoeCodes: Set[String] = Set("1", "2", "3", "6", "0", "H")
  val definedSoeDucrCodes: Set[String] = Set("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "D", "F")
  val definedSoeMucrCodes: Set[String] = Set("0", "3", "C")

  def inputCustomsStatus(code: String)(implicit messages: Messages): String =
    translate(definedIcsCodes, "ileQuery.mapping.ics", code)

  def routeOfEntry(code: String)(implicit messages: Messages): String =
    translate(definedRoeCodes, "ileQuery.mapping.roe", code)

  def statusOfEntryDucr(code: String)(implicit messages: Messages): String =
    translate(definedSoeDucrCodes, "ileQuery.mapping.soe.ducr", code)

  def statusOfEntryMucr(code: String)(implicit messages: Messages): String =
    translate(definedSoeMucrCodes, "ileQuery.mapping.soe.mucr", code)

  private def translate(defined: Set[String], messageKeyPrefix: String, code: String)(implicit messages: Messages) =
    if (defined.contains(code)) {
      messages(s"$messageKeyPrefix.$code")
    } else {
      messages(s"$messageKeyPrefix.default", code)
    }
}
