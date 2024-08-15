/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.Transport.ModesOfTransport._
import play.api.data.FormError
import play.api.libs.json.Json
import base.UnitSpec
import testdata.TestDataHelper.createRandomAlphanumericString

class TransportSpec extends UnitSpec {

  "Transport model" should {

    "has correct formId value" in {
      Transport.formId must be("Transport")
    }

    "has correct values of Mode of Transport" in {
      Sea must be("1")
      Rail must be("2")
      Road must be("3")
      Air must be("4")
      PostalOrMail must be("5")
      FixedInstallations must be("6")
      InlandWaterway must be("7")
      Other must be("8")
    }

    "contains all allowed modes of transport" in {
      Transport.allowedModesOfTransport.size must be(8)
      Transport.allowedModesOfTransport must contain(Sea)
      Transport.allowedModesOfTransport must contain(Rail)
      Transport.allowedModesOfTransport must contain(Road)
      Transport.allowedModesOfTransport must contain(Air)
      Transport.allowedModesOfTransport must contain(PostalOrMail)
      Transport.allowedModesOfTransport must contain(FixedInstallations)
      Transport.allowedModesOfTransport must contain(InlandWaterway)
      Transport.allowedModesOfTransport must contain(Other)
    }
  }

  "Transport Out of the UK mapping" should {

    "contain errors" when {

      "all 3 values are empty" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> "", "transportId" -> "")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 3
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.empty"))
        errors(1) must be(FormError("nationality", "transport.nationality.empty"))
        errors(2) must be(FormError("transportId", "transport.transportId.empty"))
      }

      "only modeOfTransport is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "", "transportId" -> "")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("nationality", "transport.nationality.empty"))
        errors(1) must be(FormError("transportId", "transport.transportId.empty"))
      }

      "only nationality is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> "GB", "transportId" -> "")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.empty"))
        errors(1) must be(FormError("transportId", "transport.transportId.empty"))
      }

      "only transportId is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> "", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.empty"))
        errors(1) must be(FormError("nationality", "transport.nationality.empty"))
      }

      "modeOfTransport is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> "GB", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("modeOfTransport", "transport.modeOfTransport.empty"))
      }

      "nationality is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("nationality", "transport.nationality.empty"))
      }

      "transportId is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "GB", "transportId" -> "")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("transportId", "transport.transportId.empty"))
      }

      "provided with 3 incorrect values" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "invalid", "transportId" -> createRandomAlphanumericString(36))
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 3
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.error"))
        errors(2) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only modeOfTransport being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "invalid", "transportId" -> "Reference!@#$%^")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("nationality", "transport.nationality.error"))
        errors(1) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only nationality being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "GB", "transportId" -> "Reference!@#$%^")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only transportId being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "invalid", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.error"))
      }

      "provided with incorrect modeOfTransport" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "GB", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
      }

      "provided with incorrect nationality" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "invalid", "transportId" -> "Reference")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("nationality", "transport.nationality.error"))
      }

      "provided with incorrect transportId" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "GB", "transportId" -> "Reference!@#$%^")
        val errors = Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("transportId", "transport.transportId.error"))
      }
    }

    "contain no errors" when {

      "all 3 values are correct" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> " GB ", "transportId" -> " Reference ")
        Transport.outOfTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "convert to upper case" when {
        "country is lower case" in {
          val form = Transport.outOfTheUkForm.bind(
            Json.obj("modeOfTransport" -> "2", "transportId" -> " xwercwrxwy ", "nationality" -> " pl "),
            JsonBindMaxChars
          )

          form.errors mustBe empty
          form.value.flatMap(_.modeOfTransport) must be(Some("2"))
          form.value.flatMap(_.transportId) must be(Some("xwercwrxwy"))
          form.value.flatMap(_.nationality) must be(Some("PL"))
        }
      }
    }
  }

  "Transport Back into the UK mapping" should {

    "contain errors" when {

      "provided with 3 correct values" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "GB", "transportId" -> "Reference")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("", "transport.backIntoTheUk.error.allFieldsEntered"))
      }

      "provided with 3 incorrect values" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "invalid", "transportId" -> createRandomAlphanumericString(36))
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 3
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.error"))
        errors(2) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only modeOfTransport being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "invalid", "transportId" -> "Reference!@#$%^")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("nationality", "transport.nationality.error"))
        errors(1) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only nationality being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "GB", "transportId" -> "Reference!@#$%^")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("transportId", "transport.transportId.error"))
      }

      "provided with only transportId being correct" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "invalid", "transportId" -> "Reference")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 2
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.error"))
      }

      "provided with incorrect modeOfTransport" in {
        val inputData = Json.obj("modeOfTransport" -> "13", "nationality" -> "GB", "transportId" -> "Reference")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
      }

      "provided with incorrect nationality" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "invalid", "transportId" -> "Reference")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("nationality", "transport.nationality.error"))
      }

      "provided with incorrect transportId" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> "GB", "transportId" -> "Reference!@#$%^")
        val errors = Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors

        errors.length mustBe 1
        errors.head must be(FormError("transportId", "transport.transportId.error"))
      }
    }

    "contain no errors" when {

      "all 3 values are empty" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> " ", "transportId" -> " ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "only modeOfTransport is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> " ", "transportId" -> " ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "only nationality is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> " GB ", "transportId" -> " ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "only transportId is entered" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> " ", "transportId" -> " Reference ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "modeOfTransport is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "", "nationality" -> " GB ", "transportId" -> " Reference ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "nationality is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> " ", "transportId" -> " Reference ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "transportId is empty" in {
        val inputData = Json.obj("modeOfTransport" -> "1", "nationality" -> " GB ", "transportId" -> " ")
        Transport.backIntoTheUkForm.bind(inputData, JsonBindMaxChars).errors mustBe empty
      }

      "convert to upper case" when {
        "country is lower case" in {
          val form = Transport.backIntoTheUkForm.bind(Json.obj("modeOfTransport" -> " 2 ", "nationality" -> " pl "), JsonBindMaxChars)
          form.errors mustBe empty
          form.value.flatMap(_.modeOfTransport) must be(Some("2"))
          form.value.flatMap(_.nationality) must be(Some("PL"))
        }
      }
    }
  }
}
