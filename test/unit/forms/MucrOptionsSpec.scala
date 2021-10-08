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
import forms.MucrOptions.CreateOrAddValues._
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, Json}
import testdata.CommonTestData.correctUcr

class MucrOptionsSpec extends UnitSpec {

  "MucrOptions form" should {

    "have correct formId value" in {
      MucrOptions.formId mustBe "MucrOptions"
    }
  }

  "MucrOptions mapping" should {

    "contain errors" when {

      "provided with an empty form" in {

        val inputData = Json.obj()
        val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

        errors mustBe Seq(FormError("createOrAdd", "mucrOptions.createAdd.value.empty"))
      }

      "provided with empty createOrAdd field" in {

        val inputData = Json.obj("createOrAdd" -> "")
        val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

        errors mustBe Seq(FormError("createOrAdd", "mucrOptions.createAdd.value.empty"))
      }

      "provided with Create and newMucr" which {
        "is empty" in {

          val inputData = Json.obj("createOrAdd" -> JsString(Create.value))
          val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

          errors mustBe Seq(FormError("newMucr", "mucrOptions.reference.value.empty"))
        }

        "is incorrect" in {

          val inputData = Json.obj("createOrAdd" -> JsString(Create.value), "newMucr" -> "!@#$%^INVALID-MUCR*&^%$#")
          val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

          errors mustBe Seq(FormError("newMucr", "mucrOptions.reference.value.error"))
        }
      }

      "provided with Add and existingMucr" which {
        "is empty" in {

          val inputData = Json.obj("createOrAdd" -> JsString(Add.value))
          val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

          errors mustBe Seq(FormError("existingMucr", "mucrOptions.reference.value.empty"))
        }

        "is incorrect" in {

          val inputData = Json.obj("createOrAdd" -> JsString(Add.value), "existingMucr" -> "!@#$%^INVALID-MUCR*&^%$#")
          val errors = MucrOptions.form.bind(inputData, JsonBindMaxChars).errors

          errors mustBe Seq(FormError("existingMucr", "mucrOptions.reference.value.error"))
        }
      }
    }

    "not contain any errors" when {

      "provided with Create and new MUCR" in {

        val inputData = MucrOptions(createOrAdd = Create, newMucr = correctUcr)
        val errors = MucrOptions.form.fillAndValidate(inputData).errors

        errors.length mustBe 0
      }

      "provided with Add and existing MUCR" in {

        val inputData = MucrOptions(createOrAdd = Add, existingMucr = correctUcr)
        val errors = MucrOptions.form.fillAndValidate(inputData).errors

        errors.length mustBe 0
      }

      "convert new mucr to upper case" in {

        val form = MucrOptions.form.bind(
          JsObject(Map("createOrAdd" -> JsString("create"), "newMucr" -> JsString("gb/abced1234-15804test"), "existingMucr" -> JsString(""))),
          JsonBindMaxChars
        )

        form.errors mustBe empty
        form.value.map(_.newMucr) mustBe Some("GB/ABCED1234-15804TEST")
      }

      "convert existing mucr to upper case" in {

        val form = MucrOptions.form.bind(
          JsObject(Map("createOrAdd" -> JsString("add"), "newMucr" -> JsString(""), "existingMucr" -> JsString("gb/abced1234-15804test"))),
          JsonBindMaxChars
        )

        form.errors mustBe empty
        form.value.map(_.existingMucr) mustBe Some("GB/ABCED1234-15804TEST")
      }
    }
  }

}
