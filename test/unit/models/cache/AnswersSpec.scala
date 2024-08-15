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

package models.cache

import forms.ConsignmentReferenceType
import models.UcrType.{Ducr, Mucr}
import models.{UcrBlock, UcrType}
import play.api.libs.json.{JsSuccess, Json}
import base.UnitSpec

import java.time.Instant
import java.util.Date

class AnswersSpec extends UnitSpec {

  "Answers reads" should {

    val providerId = "providerId"
    val ucrBlock = UcrBlock("ucr", UcrType.Ducr)

    "correctly read updated field" when {

      "the value is a date" in {
        val date = new Date()
        val expectedInstant = date.toInstant

        val cacheJson = Json.obj(
          "providerId" -> providerId,
          "queryUcr" -> Json.toJson(ucrBlock),
          "updated" -> Json.obj(s"$$date" -> Json.obj(s"$$numberLong" -> date.getTime.toString))
        )

        val expectedResult = Cache(providerId, None, Some(ucrBlock), Some(expectedInstant))

        Cache.format.reads(cacheJson) mustBe JsSuccess(expectedResult)
      }
    }

    "correctly write updated field as a date" in {
      val instant = Instant.now()
      val cache = Cache(providerId, None, Some(ucrBlock), Some(instant))

      val expectedJson = Json.obj(
        "providerId" -> providerId,
        "queryUcr" -> Json.toJson(ucrBlock),
        "updated" -> Json.obj(s"$$date" -> Json.obj(s"$$numberLong" -> instant.toEpochMilli.toString))
      )

      Cache.format.writes(cache) mustBe expectedJson
    }

    "correctly read Arrival Answers" in {
      val arrivalAnswersJson = Json.obj("type" -> JourneyType.ARRIVE.toString)
      val expectedResult = ArrivalAnswers(None)
      Answers.format.reads(arrivalAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Departure Answers" in {
      val departureAnswersJson = Json.obj("type" -> JourneyType.DEPART.toString)
      val expectedResult = DepartureAnswers(None)
      Answers.format.reads(departureAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Associate UCR Answers" in {
      val associateUcrAnswersJson = Json.obj("type" -> JourneyType.ASSOCIATE_UCR.toString)
      val expectedResult = AssociateUcrAnswers(None)
      Answers.format.reads(associateUcrAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Disassociate UCR Answers" in {
      val dissociateUcrAnswersJson = Json.obj("type" -> JourneyType.DISSOCIATE_UCR.toString)
      val expectedResult = DisassociateUcrAnswers(None)
      Answers.format.reads(dissociateUcrAnswersJson) mustBe JsSuccess(expectedResult)
    }

    "correctly read Shut Mucr Answers" in {
      val shutMucrAnswersJson = Json.obj("type" -> JourneyType.SHUT_MUCR.toString)
      val expectedResult = ShutMucrAnswers(None)
      Answers.format.reads(shutMucrAnswersJson) mustBe JsSuccess(expectedResult)
    }
  }

  "Answers build fromQueryUcr" should {
    val ducrQuery = UcrBlock(ucr = "ducr", ucrType = Ducr)
    val mucrQuery = UcrBlock(ucr = "mucr", ucrType = Mucr)

    "correctly create ArrivalAnswers from ducr query" in {
      val answer = ArrivalAnswers.fromQueryUcr(Some(ducrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("ducr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.D)
    }

    "correctly create ArrivalAnswers from mucr query" in {
      val answer = ArrivalAnswers.fromQueryUcr(Some(mucrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("mucr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.M)
    }

    "correctly create ArrivalAnswers from no query" in {
      val answer = ArrivalAnswers.fromQueryUcr(None)
      answer.consignmentReferences mustBe None
    }

    "correctly create RetrospectiveArrivalAnswers from ducr query" in {
      val answer = RetrospectiveArrivalAnswers.fromQueryUcr(Some(ducrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("ducr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.D)
    }

    "correctly create RetrospectiveArrivalAnswers from mucr query" in {
      val answer = RetrospectiveArrivalAnswers.fromQueryUcr(Some(mucrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("mucr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.M)
    }

    "correctly create RetrospectiveArrivalAnswers from no query" in {
      val answer = RetrospectiveArrivalAnswers.fromQueryUcr(None)
      answer.consignmentReferences mustBe None
    }

    "correctly create DepartureAnswers from ducr query" in {
      val answer = DepartureAnswers.fromQueryUcr(Some(ducrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("ducr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.D)
    }

    "correctly create DepartureAnswers from mucr query" in {
      val answer = DepartureAnswers.fromQueryUcr(Some(mucrQuery))
      answer.consignmentReferences.map(_.referenceValue) mustBe Some("mucr")
      answer.consignmentReferences.map(_.reference) mustBe Some(ConsignmentReferenceType.M)
    }

    "correctly create DepartureAnswers from no query" in {
      val answer = DepartureAnswers.fromQueryUcr(None)
      answer.consignmentReferences mustBe None
    }

    "correctly create AssociateUcrAnswers from ducr query" in {
      val answer = AssociateUcrAnswers.fromQueryUcr(Some(ducrQuery))
      answer.childUcr.map(_.ucr) mustBe Some("ducr")
      answer.childUcr.map(_.kind) mustBe Some(UcrType.Ducr)
    }

    "correctly create AssociateUcrAnswers from mucr query" in {
      val answer = AssociateUcrAnswers.fromQueryUcr(Some(mucrQuery))
      answer.childUcr.map(_.ucr) mustBe Some("mucr")
      answer.childUcr.map(_.kind) mustBe Some(UcrType.Mucr)
    }

    "correctly create AssociateUcrAnswers from no query" in {
      val answer = AssociateUcrAnswers.fromQueryUcr(None)
      answer.childUcr mustBe None
    }

    "correctly create DisassociateUcrAnswers from ducr query" in {
      val answer = DisassociateUcrAnswers.fromQueryUcr(Some(ducrQuery))
      answer.ucr.map(_.ucr) mustBe Some("ducr")
      answer.ucr.map(_.kind) mustBe Some(UcrType.Ducr)
    }

    "correctly create DisassociateUcrAnswers from mucr query" in {
      val answer = DisassociateUcrAnswers.fromQueryUcr(Some(mucrQuery))
      answer.ucr.map(_.ucr) mustBe Some("mucr")
      answer.ucr.map(_.kind) mustBe Some(UcrType.Mucr)
    }

    "correctly create DisassociateUcrAnswers from no query" in {
      val answer = DisassociateUcrAnswers.fromQueryUcr(None)
      answer.ucr mustBe None
    }

    "correctly create ShutMucrAnswers from ducr query" in {
      val answer = ShutMucrAnswers.fromQueryUcr(Some(ducrQuery))
      answer.shutMucr mustBe None
    }

    "correctly create ShutMucrAnswers from mucr query" in {
      val answer = ShutMucrAnswers.fromQueryUcr(Some(mucrQuery))
      answer.shutMucr.map(_.mucr) mustBe Some("mucr")
    }

    "correctly create ShutMucrAnswers from no query" in {
      val answer = ShutMucrAnswers.fromQueryUcr(None)
      answer.shutMucr mustBe None
    }
  }
}
