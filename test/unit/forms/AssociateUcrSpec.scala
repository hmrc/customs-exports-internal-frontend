/*
 * Copyright 2021 HM Revenue & Customs
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

class AssociateUcrSpec extends UnitSpec {

  "AssociateUcr" should {

    "convert to upper case" when {
      "provided with Ducr" in {

        val form = AssociateUcr.form.bind(JsObject(Map("kind" -> JsString("ducr"), "ducr" -> JsString("8gb123457359100-test0001"))))

        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("8GB123457359100-TEST0001"))
      }

      "provided with Mucr" in {

        val form = AssociateUcr.form.bind(JsObject(Map("kind" -> JsString("mucr"), "mucr" -> JsString("gb/abced1234-15804test"))))

        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("GB/ABCED1234-15804TEST"))
      }

      "provided with Mucr that is 35 characters long" in {

        val form = AssociateUcr.form.bind(JsObject(Map("kind" -> JsString("mucr"), "mucr" -> JsString("gb/abced1234-15804test1234567890123"))))

        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("GB/ABCED1234-15804TEST1234567890123"))
      }
    }
  }
  "return an error" when {
    "provided with Mucr that is over 35 characters long" in {

      val form = AssociateUcr.form.bind(JsObject(Map("kind" -> JsString("mucr"), "mucr" -> JsString("gb/abced1234-15804test12345678901234"))))

      form.errors mustBe Seq(FormError("mucr", List("mucr.error.format")))
    }
  }
}
