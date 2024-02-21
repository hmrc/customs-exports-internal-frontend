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

import base.UnitSpec
import models.{UcrBlock, UcrType}
import play.api.data.FormError
import testdata.CommonTestData.{validDucr, validDucrPartId, validMucr, validWholeDucrParts}

class ChiefConsignmentSpec extends UnitSpec {

  private val mucrToTrim = s" $validMucr "
  private val ducrToTrim = s" $validDucr "
  private val ducrPartIdToTrim = s" $validDucrPartId "

  "ChiefConsignment mapping" should {

    "return errors" when {
      val expectedAllEmptyError = FormError("", Seq("manageChiefConsignment.error.blank"))
      val expectedMucrError = FormError("mucr", Seq("manageChiefConsignment.mucr.error"))
      val expectedDucrError = FormError("ducr", Seq("manageChiefConsignment.ducr.error"))
      val expectedDucrPartIdError = FormError("ducrPartId", Seq("manageChiefConsignment.ducrPartId.error"))
      val expectedMismatchError = FormError("", Seq("manageChiefConsignment.error.mismatchedInput"))
      val expectedMissingDucrError = FormError("ducr", Seq("manageChiefConsignment.ducr.error.blank"))

      "provided with all empty input" in {
        val input = Map("mucr" -> "", "ducr" -> "", "ducrPartId" -> "")
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedAllEmptyError)
      }

      "provided with incorrect DUCR" in {
        val input = Map("ducr" -> "incorrect!@#$%^", "ducrPartId" -> "M")
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedDucrError)
      }

      "provided with incorrect DUCR Part ID" in {
        val input = Map("ducr" -> "3GB986007773125-INVOICE123", "ducrPartId" -> "incorrect!@#$%^")
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedDucrPartIdError)
      }

      "provided with incorrect MUCR" in {
        val input = Map("mucr" -> "incorrect!@#$%^")
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedMucrError)
      }

      "provided with a DUCR Part ID and MUCR at the same time" in {
        val input = Map("mucr" -> mucrToTrim, "ducrPartId" -> ducrPartIdToTrim)
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedMismatchError)
      }

      "provided with a DUCR and MUCR at the same time" in {
        val input = Map("mucr" -> mucrToTrim, "ducr" -> ducrToTrim)
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedMismatchError)
      }

      "provided with a DUCR Part ID but no DUCR" in {
        val input = Map("ducrPartId" -> ducrPartIdToTrim)
        val result = ChiefConsignment.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 1
        result.swap.toOption.get must contain(expectedMissingDucrError)
      }
    }

    "return no errors" when {

      "provided with correct MUCR" in {
        val input = Map("mucr" -> mucrToTrim)
        val result = ChiefConsignment.mapping.bind(input)
        result.isRight mustBe true
      }

      "provided with correct both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducrToTrim, "ducrPartId" -> ducrPartIdToTrim)
        val result = ChiefConsignment.mapping.bind(input)
        result.isRight mustBe true
      }

      "provided with correct lower cased both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = ChiefConsignment.mapping.bind(input)
        result.isRight mustBe true
      }
    }

    "convert to upper case" when {

      "provided with MUCR containing lower case characters" in {
        val input = Map("mucr" -> mucrToTrim.toLowerCase)
        val result = ChiefConsignment.mapping.bind(input)

        result.isRight mustBe true
        result.toOption.get.mucr.get mustBe validMucr.toUpperCase
      }

      "provided with DUCR containing lower case characters" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = ChiefConsignment.mapping.bind(input)

        result.isRight mustBe true
        result.toOption.get.ducr.get mustBe validDucr.toUpperCase
      }

      "provided with DUCR Part ID containing lower case characters" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = ChiefConsignment.mapping.bind(input)

        result.isRight mustBe true
        result.toOption.get.ducrPartId.get mustBe validDucrPartId.toUpperCase
      }
    }
  }

  "ChiefConsignment on toUcrBlock" should {

    "return UcrBlock with correct type field for MUCR" in {
      val mucrDetails = ChiefConsignment(mucr = Some(validMucr), ducr = None, ducrPartId = None)
      val expectedType = UcrType.Mucr.codeValue
      mucrDetails.toUcrBlock.ucrType mustBe expectedType
    }

    "return UcrBlock with correct ucr field for MUCR" in {
      val ducrDetails = ChiefConsignment(mucr = Some(validMucr), ducr = None, ducrPartId = None)
      val expectedUcr = validMucr
      ducrDetails.toUcrBlock.ucr mustBe expectedUcr
    }

    "return UcrBlock with correct type field for DUCR without Part" in {
      val ducrDetails = ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = None)
      val expectedType = UcrType.Ducr.codeValue
      ducrDetails.toUcrBlock.ucrType mustBe expectedType
    }

    "return UcrBlock with correct ucr field for DUCR without Part" in {
      val manageChiefConsignment = ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = None)
      val expectedUcr = validDucr
      manageChiefConsignment.toUcrBlock.ucr mustBe expectedUcr
    }

    "return UcrBlock with correct type field for DUCR with Part" in {
      val manageChiefConsignment = ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = Some(validDucrPartId))
      val expectedType = UcrType.DucrPart.codeValue
      manageChiefConsignment.toUcrBlock.ucrType mustBe expectedType
    }

    "return UcrBlock with correct ucr field for DUCR with Part" in {
      val manageChiefConsignment = ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = Some(validDucrPartId))
      val expectedUcr = validWholeDucrParts
      manageChiefConsignment.toUcrBlock.ucr mustBe expectedUcr
    }
  }

  "ChiefConsignment on apply" should {

    "throw IllegalArgumentException" when {
      "provided with UcrBlock of some unknown type" in {
        val ucrBlock = UcrBlock(ucr = "nonsense", ucrType = "F")
        intercept[IllegalArgumentException](ChiefConsignment(ucrBlock))
      }
    }

    "return ChiefConsignment with correct mucr" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.Mucr, ucr = validMucr)
      val expectedMucr = validMucr
      ChiefConsignment(ucrBlock).mucr.get mustBe expectedMucr
    }

    "return ChiefConsignment with correct ducr" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucr = validDucr
      ChiefConsignment(ucrBlock).ducr.get mustBe expectedDucr
    }

    "return ChiefConsignment with correct ducrPartId" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucrPartId = validDucrPartId
      ChiefConsignment(ucrBlock).ducrPartId.get mustBe expectedDucrPartId
    }
  }
}
