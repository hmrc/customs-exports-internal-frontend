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

package controllers

import controllers.routes.ChoiceController
import forms.ChiefConsignment
import models.{UcrBlock, UcrType}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import models.cache.Cache
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.CommonTestData.{validDucr, validDucrPartId, validMucr, validWholeDucrParts}
import views.html.manage_chief_consignment

import scala.concurrent.ExecutionContext.global

class ManageChiefConsignmentControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures with IntegrationPatience {

  private val manageChiefConsignmentPage = mock[manage_chief_consignment]

  private def controller =
    new ManageChiefConsignmentController(stubMessagesControllerComponents(), SuccessfulAuth(), cacheRepository, manageChiefConsignmentPage)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(manageChiefConsignmentPage)
    when(manageChiefConsignmentPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(manageChiefConsignmentPage)
    super.afterEach()
  }

  "ManageChiefConsignmentController on displayPage" should {

    "call CacheRepository" in {
      givenTheCacheIsEmpty()
      controller.displayPage(getRequest).futureValue
      verify(cacheRepository).findByProviderId(any())
    }

    "return Ok (200) response" in {
      givenTheCacheIsEmpty()
      val result = controller.displayPage(getRequest)
      status(result) mustBe OK
    }
  }

  "ManageChiefConsignmentController on displayPage" when {

    "cache is empty" should {

      "call ChiefConsignment view" in {
        givenTheCacheIsEmpty()
        controller.displayPage(getRequest).futureValue
        verify(manageChiefConsignmentPage).apply(any())(any(), any())
      }

      "pass empty form to ChiefConsignment view" in {
        givenTheCacheIsEmpty()
        controller.displayPage(getRequest).futureValue

        val expectedForm = ChiefConsignment.form()
        verify(manageChiefConsignmentPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "cache contains queryUcr of DucrParts type" should {

      "call ChiefConsignment view" in {
        val cacheContents =
          Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.DucrPart.codeValue, ucr = validWholeDucrParts)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue
        verify(manageChiefConsignmentPage).apply(any())(any(), any())
      }

      "pass data from CacheRepository to ChiefConsignment view" in {
        val cacheContents =
          Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.DucrPart.codeValue, ucr = validWholeDucrParts)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue

        val expectedForm = ChiefConsignment.form().fill(ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = Some(validDucrPartId)))
        verify(manageChiefConsignmentPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "cache contains queryUcr of DUCR only" should {

      "call ChiefConsignment view" in {
        val cacheContents = Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue
        verify(manageChiefConsignmentPage).apply(any())(any(), any())
      }

      "pass populated form to ChiefConsignment view" in {
        val cacheContents = Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue

        val expectedForm = ChiefConsignment.form().fill(ChiefConsignment(mucr = None, ducr = Some(validDucr), ducrPartId = None))
        verify(manageChiefConsignmentPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "cache contains queryUcr of MUCR only" should {

      "call ChiefConsignment view" in {
        val cacheContents = Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Mucr.codeValue, ucr = validMucr)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue
        verify(manageChiefConsignmentPage).apply(any())(any(), any())
      }

      "pass populated form to ChiefConsignment view" in {
        val cacheContents = Cache(providerId = "12345", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Mucr.codeValue, ucr = validMucr)))
        givenTheCacheContains(cacheContents)
        controller.displayPage(getRequest).futureValue

        val expectedForm = ChiefConsignment.form().fill(ChiefConsignment(mucr = Some(validMucr), ducr = None, ducrPartId = None))
        verify(manageChiefConsignmentPage).apply(meq(expectedForm))(any(), any())
      }
    }
  }

  "ManageChiefConsignmentController on submitChiefConsignment" when {

    val invalidFormData =
      Seq(Json.obj("ducr" -> "InvalidDucr!@#", "ducrPartId" -> "InvalidDucrPartId!@#"), Json.obj("ducr" -> validDucr, "mucr" -> validMucr))

    invalidFormData.foreach { inputData =>
      s"provided with incorrect data $inputData" should {

        "return BadRequest (400) response" in {
          val result = controller.submitChiefConsignment()(postRequest(inputData))
          status(result) mustBe BAD_REQUEST
        }

        "not call CacheRepository" in {
          controller.submitChiefConsignment()(postRequest(inputData)).futureValue
          verifyZeroInteractions(cacheRepository)
        }
      }
    }

    val ducrPartData = Json.obj("ducr" -> validDucr, "ducrPartId" -> validDucrPartId)
    val ducrData = Json.obj("ducr" -> validDucr)
    val mucrData = Json.obj("mucr" -> validMucr)
    val validFormData = Seq(ducrData, ducrPartData, mucrData)

    validFormData.foreach { inputData =>
      s"provided with correct data $inputData" should {

        "call CacheRepository upsert" in {
          controller.submitChiefConsignment()(postRequest(inputData)).futureValue
          verify(cacheRepository).upsert(any[Cache])
        }

        "provide CacheRepository with correct UcrBlock object" in {
          controller.submitChiefConsignment()(postRequest(inputData)).futureValue

          val expectedUcrBlock = inputData match {
            case _ if ducrData == inputData => UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr.toUpperCase, chiefUcr = Some(true))
            case _ if ducrPartData == inputData =>
              UcrBlock(ucrType = UcrType.DucrPart.codeValue, ucr = validWholeDucrParts.toUpperCase, chiefUcr = Some(true))
            case _ if mucrData == inputData => UcrBlock(ucrType = UcrType.Mucr.codeValue, ucr = validMucr.toUpperCase, chiefUcr = Some(true))
            case _                          => throw new IllegalArgumentException(s"Unexpected input data: $inputData")
          }

          theCacheUpserted.queryUcr mustBe defined
          theCacheUpserted.queryUcr.get mustBe expectedUcrBlock
        }

        "return SeeOther (303) response" in {
          val result = controller.submitChiefConsignment()(postRequest(inputData))
          status(result) mustBe SEE_OTHER
        }

        "redirect to Choice page" in {
          val result = controller.submitChiefConsignment()(postRequest(inputData))
          redirectLocation(result) mustBe Some(ChoiceController.displayPage.url)
        }
      }
    }
  }
}
