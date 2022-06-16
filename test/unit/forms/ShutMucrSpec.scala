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

package forms

import base.UnitSpec
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class ShutMucrSpec extends UnitSpec {

  "ShutMucr model" should {

    "have correct formId value" in {

      ShutMucr.formId must be("ShutMucr")
    }
  }

  "ShutMucr mapping" should {

    "contain error" when {

      "mucr is empty" in {

        val inputData = ShutMucr("")
        val errors = ShutMucr.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("mucr", "error.mucr.empty"))
      }

      "mucr is invalid" in {

        val inputData = ShutMucr("XXAUcorrect")
        val errors = ShutMucr.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("mucr", "error.mucr.format"))
      }

      "MUCR length is over 35 characters long" in {

        val errors = ShutMucr.form().bind(JsObject(Map("mucr" -> JsString("gb/abced1234-15804test12345678901234"))), JsonBindMaxChars).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.format"))
      }
    }

    "not contain any errors" when {

      "data is correct" in {

        val inputData = ShutMucr("GB/ABC-12345")
        val errors = ShutMucr.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }

    "convert to upper case" when {

      "MUCR is on lower case" in {
        val form = ShutMucr.form().bind(JsObject(Map("mucr" -> JsString("gb/abced1234-15804test"))), JsonBindMaxChars)

        form.errors mustBe empty
        form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST"))
      }

      "MUCR is on lower case and is 35 characters long" in {
        val form = ShutMucr.form().bind(JsObject(Map("mucr" -> JsString("gb/abced1234-15804test1234567890123"))), JsonBindMaxChars)

        form.errors mustBe empty
        form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST1234567890123"))
      }
    }
  }
}
