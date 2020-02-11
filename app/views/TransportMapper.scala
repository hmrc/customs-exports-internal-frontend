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

import javax.inject.Singleton
import models.notifications.queries.Transport
import services.Countries
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Empty, Text}

@Singleton
class TransportMapper {

  private def countryName(code: String) = Countries.allCountries.find(_.countryCode == code).map(_.countryName).getOrElse(code)

  def transportHtml(transport: Transport): Content =
    (transport.transportId, transport.nationality) match {
      case (Some(id), Some(nationality)) => Text(s"$id, ${countryName(nationality)}")
      case (Some(id), None)              => Text(s"$id")
      case (None, Some(nationality))     => Text(s"${countryName(nationality)}")
      case _                             => Empty
    }

}
