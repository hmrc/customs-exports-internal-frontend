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

package unit.models.cache

import models.cache._
import play.api.libs.json.{JsObject, JsString, JsSuccess, JsValue}
import unit.base.UnitSpec

class Answers extends UnitSpec {

  "Answers reads" should {

    "correctly read Arrival Answers" in {

      val arrivalAnswersJson: JsValue =
        JsObject(Map("type" -> JsString(JourneyType.ARRIVE.toString)))

      val expectedResult = ArrivalAnswers(None)

      Answers.format.reads(arrivalAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Departure Answers" in {

      val departureAnswersJson: JsValue =
        JsObject(Map("type" -> JsString(JourneyType.DEPART.toString)))

      val expectedResult = DepartureAnswers(None)

      Answers.format.reads(departureAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Associate UCR Answers" in {

      val associateUcrAnswersJson: JsValue =
        JsObject(Map("type" -> JsString(JourneyType.ASSOCIATE_UCR.toString)))

      val expectedResult = AssociateUcrAnswers(None)

      Answers.format.reads(associateUcrAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Dissociate UCR Answers" in {

      val dissociateUcrAnswersJson: JsValue =
        JsObject(Map("type" -> JsString(JourneyType.DISSOCIATE_UCR.toString)))

      val expectedResult = DisassociateUcrAnswers(None)

      Answers.format.reads(dissociateUcrAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Shut Mucr Answers" in {

      val shutMucrAnswersJson: JsValue =
        JsObject(Map("type" -> JsString(JourneyType.SHUT_MUCR.toString)))

      val expectedResult = ShutMucrAnswers(None)

      Answers.format.reads(shutMucrAnswersJson) mustBe JsSuccess(expectedResult)
    }
  }
}
