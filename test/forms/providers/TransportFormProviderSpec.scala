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
import base.UnitSpec

class TransportFormProviderSpec extends UnitSpec {

  private val formProvider = new TransportFormProvider()

  "TransportFormProvider" should {

    "return Out of the UK form" when {
      "provided with answers containing OutOfTheUk value for GoodsDeparted" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(OutOfTheUk)))
        val form = formProvider.provideForm(answers)
        form mustBe Transport.outOfTheUkForm
      }
    }

    "return Back into the UK form" when {
      "provided with answers containing BackIntoTheUk value for GoodsDeparted" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(BackIntoTheUk)))
        val form = formProvider.provideForm(answers)
        form mustBe Transport.backIntoTheUkForm
      }
    }
  }
}
