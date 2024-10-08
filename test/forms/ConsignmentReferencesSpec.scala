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

package forms

import play.api.data.FormError
import play.api.libs.json.Json
import base.UnitSpec

class ConsignmentReferencesSpec extends UnitSpec {

  val validDucr = " 9GB123456 "
  val validMucr = " GB/ABC-12342 "

  "Consignment References mapping" should {

    "return errors for empty fields" in {
      val errors = ConsignmentReferences.form().bind(Map("reference" -> "x", "ducrValue" -> "", "mucrValue" -> "")).errors
      errors must be(Seq(FormError("reference", "consignmentReferences.reference.error")))
    }

    "no errors for complete fields " in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.M, validMucr)
      ConsignmentReferences.form().fillAndValidate(inputData).errors mustBe empty
    }

    "have error for missing Ducr" in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.D, "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors
      errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.empty")))
    }

    "have error for missing Mucr" in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.M, "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors
      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.empty")))
    }

    "have error for invalid Ducr" in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.D, "ABC")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors
      errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.error")))
    }

    "have error for invalid Mucr" in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.M, "ABC")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors
      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.error")))
    }

    "have error for Mucr length > 35 characters" in {
      val inputData = ConsignmentReferences(ConsignmentReferenceType.M, "GB/82F9-0N2F6500040010TO120P0A300689")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors
      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.error")))
    }

    "convert ducr to upper case" in {
      val form = ConsignmentReferences.form().bind(Json.obj("reference" -> "D", "ducrValue" -> " 8gb123457359100-test0001 "), JsonBindMaxChars)
      form.errors mustBe empty
      form.value.map(_.referenceValue) must be(Some("8GB123457359100-TEST0001"))
    }

    "convert mucr to upper case" in {
      val form = ConsignmentReferences.form().bind(Json.obj("reference" -> "M", "mucrValue" -> " gb/abced1234-15804test "), JsonBindMaxChars)
      form.errors mustBe empty
      form.value.map(_.referenceValue) must be(Some("GB/ABCED1234-15804TEST"))
    }

    "convert mucr that is 35 characters long to upper case" in {
      val form =
        ConsignmentReferences.form().bind(Json.obj("reference" -> "M", "mucrValue" -> " gb/abced1234-15804test1234567890123 "), JsonBindMaxChars)
      form.errors mustBe empty
      form.value.map(_.referenceValue) must be(Some("GB/ABCED1234-15804TEST1234567890123"))
    }
  }
}
