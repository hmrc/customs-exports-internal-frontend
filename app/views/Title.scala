/*
 * Copyright 2022 HM Revenue & Customs
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
import views.Title.NO_SECTION

case class Title(headingKey: String, sectionKey: String = NO_SECTION, headingArgs: Seq[String] = Seq.empty) {

  def format(implicit messages: Messages): String = {
    val heading = messages(headingKey, headingArgs: _*)
    val service = messages("service.name")

    if (sectionKey.nonEmpty) {
      messages("title.withSection.format", heading, messages(sectionKey), service)
    } else {
      messages("title.format", heading, service)
    }

  }
}

object Title {
  val NO_SECTION = ""
}
