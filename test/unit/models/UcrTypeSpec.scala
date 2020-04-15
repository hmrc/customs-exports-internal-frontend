/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import base.UnitSpec
import play.api.libs.json.{JsString, JsSuccess}

class UcrTypeSpec extends UnitSpec {

  "UcrType" should {

    "be correctly written to JSON" when {

      "it is DUCR" in {

        val ucrType = UcrType.Ducr
        val expectedJson = JsString("ducr")

        UcrType.format.writes(ucrType) mustBe expectedJson
      }

      "it is MUCR" in {

        val ucrType = UcrType.Mucr
        val expectedJson = JsString("mucr")

        UcrType.format.writes(ucrType) mustBe expectedJson
      }
    }

    "be correctly read from JSON" when {

      "it is DUCR" in {

        val json = JsString("ducr")
        val expectedUcrType = UcrType.Ducr

        UcrType.format.reads(json) mustBe JsSuccess(expectedUcrType)
      }

      "it is MUCR" in {

        val json = JsString("mucr")
        val expectedUcrType = UcrType.Mucr

        UcrType.format.reads(json) mustBe JsSuccess(expectedUcrType)
      }
    }
  }
}
