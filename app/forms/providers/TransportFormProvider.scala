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

package forms.providers

import forms.GoodsDeparted.DepartureLocation.{BackIntoTheUk, OutOfTheUk}
import forms.{GoodsDeparted, Transport}
import models.cache.DepartureAnswers
import play.api.data.Form

class TransportFormProvider {

  def provideForm(answers: DepartureAnswers): Form[Transport] = answers.goodsDeparted match {
    case Some(GoodsDeparted(OutOfTheUk))    => Transport.outOfTheUkForm
    case Some(GoodsDeparted(BackIntoTheUk)) => Transport.backIntoTheUkForm
    case _                                  => throw new IllegalArgumentException(s"No GoodsDeparted provided in $answers")
  }
}
