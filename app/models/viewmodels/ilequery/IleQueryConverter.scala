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

package models.viewmodels.ilequery

import javax.inject.{Inject, Singleton}
import models.notifications.queries.Transport
import models.viewmodels.decoder.{CodeWithMessageKey, Decoder, ROECode}
import play.api.i18n.Messages
import services.Countries.countryName
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Empty, HtmlContent, Text}

@Singleton
class IleQueryConverter @Inject()(decoder: Decoder) {

  private def htmlString(codeWithMessageKey: CodeWithMessageKey)(implicit messages: Messages) =
    s"<strong>${codeWithMessageKey.code}</strong> - ${messages(codeWithMessageKey.messageKey)}"

  def inputCustomsStatus(code: String)(implicit messages: Messages): Content =
    decoder.ics(code).map(icsCode => HtmlContent(htmlString(icsCode))).getOrElse(Empty)

  def routeOfEntry(roe: ROECode)(implicit messages: Messages): Content = roe match {
    case ROECode.UnknownRoe => Text(messages("ileQueryResponse.route.unknown"))
    case _                  => HtmlContent(htmlString(roe))
  }

  def statusOfEntryDucr(code: String)(implicit messages: Messages): Content =
    decoder.ducrSoe(code).map(soeCode => HtmlContent(htmlString(soeCode))).getOrElse(Empty)

  def statusOfEntryMucr(code: String)(implicit messages: Messages): Content =
    decoder.mucrSoe(code).map(soeCode => HtmlContent(htmlString(soeCode))).getOrElse(Empty)

  def statusOfEntryAll(code: String)(implicit messages: Messages): Content =
    decoder.allSoe(code).map(soeCode => HtmlContent(htmlString(soeCode))).getOrElse(Empty)

  def transport(transport: Transport): Content =
    Text((transport.transportId.toSeq ++ transport.nationality.map(countryName).toSeq).mkString(", "))
}
