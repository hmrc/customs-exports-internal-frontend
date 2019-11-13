/*
 * Copyright 2019 HM Revenue & Customs
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

package forms

import base.UnitSpec
import forms.GoodsDeparted.DepartureLocation.{BackIntoTheUk, OutOfTheUk}
import play.api.data.FormError
import play.api.libs.json.Json

class GoodsDepartedSpec extends UnitSpec {

  "GoodsDeparted form" should {

    "have correct formId value" in {

      GoodsDeparted.formId mustBe "GoodsDeparted"
    }
  }

  "GoodsDeparted mapping" should {

    "contain errors" when {

      "provided with empty departure location" in {

        val input = Json.obj("departureLocation" -> "")
        val errors = GoodsDeparted.form.bind(input).errors

        errors mustBe Seq(FormError("departureLocation", "goodsDeparted.departureLocation.error.empty"))
      }

      "provided with incorrect departure location" in {

        val input = Json.obj("departureLocation" -> "INVALID")
        val errors = GoodsDeparted.form.bind(input).errors

        errors mustBe Seq(FormError("departureLocation", "goodsDeparted.departureLocation.error.incorrect"))
      }
    }

    "not contain any errors" when {

      "provided with OutOfTheUk departure location" in {

        val input = Json.obj("departureLocation" -> OutOfTheUk.value)
        val errors = GoodsDeparted.form.bind(input).errors

        errors mustBe empty
      }

      "provided with BackIntoTheUk departure location" in {

        val input = Json.obj("departureLocation" -> BackIntoTheUk.value)
        val errors = GoodsDeparted.form.bind(input).errors

        errors mustBe empty
      }
    }
  }

}
