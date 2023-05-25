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
import testdata.CommonTestData.{validDucr, validDucrPartId, validWholeDucrParts}

class DucrPartDetailsSpec extends UnitSpec {

  private val ducrToTrim = s" $validDucr "
  private val ducrPartIdToTrim = s" $validDucrPartId "

  "DucrPartDetails mapping" should {

    "return errors" when {
      val expectedDucrError = FormError("ducr", Seq("ducrPartDetails.ducr.error"))
      val expectedDucrPartIdError = FormError("ducrPartId", Seq("ducrPartDetails.ducrPartId.error"))

      "provided with empty DUCR" in {
        val input = Map("ducr" -> "", "ducrPartId" -> "")
        val result = DucrPartDetails.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 2
        result.swap.toOption.get must contain(expectedDucrError)
      }

      "provided with incorrect DUCR" in {
        val input = Map("ducr" -> "incorrect!@#$%^", "ducrPartId" -> "incorrect!@#$%^")
        val result = DucrPartDetails.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 2
        result.swap.toOption.get must contain(expectedDucrError)
      }

      "provided with empty DUCR Part ID" in {
        val input = Map("ducr" -> "", "ducrPartId" -> "")
        val result = DucrPartDetails.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 2
        result.swap.toOption.get must contain(expectedDucrPartIdError)
      }

      "provided with incorrect DUCR Part ID" in {
        val input = Map("ducr" -> "incorrect!@#$%^", "ducrPartId" -> "incorrect!@#$%^")
        val result = DucrPartDetails.mapping.bind(input)

        result.isLeft mustBe true
        result.swap.toOption.get.size mustBe 2
        result.swap.toOption.get must contain(expectedDucrPartIdError)
      }
    }

    "return no errors" when {

      "provided with correct both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducrToTrim, "ducrPartId" -> ducrPartIdToTrim)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
      }

      "provided with correct lower cased both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
      }
    }

    "convert to upper case" when {

      "provided with DUCR containing lower case characters" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)

        result.isRight mustBe true
        result.toOption.get.ducr mustBe validDucr.toUpperCase
      }

      "provided with DUCR Part ID containing lower case characters" in {
        val input = Map("ducr" -> ducrToTrim.toLowerCase, "ducrPartId" -> ducrPartIdToTrim.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)

        result.isRight mustBe true
        result.toOption.get.ducrPartId mustBe validDucrPartId.toUpperCase
      }
    }
  }

  "DucrPartDetails on toUcrBlock" should {

    "return UcrBlock with correct type field" in {
      val ducrPartDetails = DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId)
      val expectedType = UcrType.DucrPart.codeValue
      ducrPartDetails.toUcrBlock.ucrType mustBe expectedType
    }

    "return UcrBlock with correct ucr field" in {
      val ducrPartDetails = DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId)
      val expectedUcr = validWholeDucrParts
      ducrPartDetails.toUcrBlock.ucr mustBe expectedUcr
    }
  }

  "DucrPartDetails on apply" should {

    "throw IllegalArgumentException" when {
      "provided with UcrBlock of type different than DucrParts" in {
        val ucrBlock = UcrBlock(ucrType = UcrType.Ducr, ucr = validDucr)
        intercept[IllegalArgumentException](DucrPartDetails(ucrBlock))
      }
    }

    "return DucrPartDetails with correct ducr" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucr = validDucr
      DucrPartDetails(ucrBlock).ducr mustBe expectedDucr
    }

    "return DucrPartDetails with correct ducrPartId" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucrPartId = validDucrPartId
      DucrPartDetails(ucrBlock).ducrPartId mustBe expectedDucrPartId
    }
  }
}
