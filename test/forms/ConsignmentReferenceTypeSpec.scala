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

import forms.ConsignmentReferenceType.ConsignmentReferenceType
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Format, JsString, JsSuccess, Json}

class ConsignmentReferenceTypeSpec extends AnyWordSpec with Matchers {

  "Type" should {
    implicit val fmt: Format[ConsignmentReferenceType] = ConsignmentReferenceType.format

    "convert to JSON" in {
      Json.toJson(ConsignmentReferenceType.D) mustBe JsString("D")
      Json.toJson(ConsignmentReferenceType.M) mustBe JsString("M")
    }

    "convert from JSON" in {
      Json.fromJson(JsString("D")) mustBe JsSuccess(ConsignmentReferenceType.D)
      Json.fromJson(JsString("M")) mustBe JsSuccess(ConsignmentReferenceType.M)
    }
  }

}
