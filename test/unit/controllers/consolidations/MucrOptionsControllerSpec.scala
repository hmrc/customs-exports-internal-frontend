/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.consolidations

import controllers.ControllerLayerSpec
import forms.ManageMucrChoice.AssociateAnotherUcrToThis
import forms.{ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.UcrType.Ducr
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.CommonTestData.validDucr
import views.html.associateucr.mucr_options

import scala.concurrent.ExecutionContext.global

class MucrOptionsControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures {

  private val page = mock[mucr_options]

  private def controller(answers: AssociateUcrAnswers = AssociateUcrAnswers(), queryUcr: Option[UcrBlock] = None) =
    new MucrOptionsController(SuccessfulAuth(), ValidJourney(answers, queryUcr), stubMessagesControllerComponents(), cacheRepository, page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  private def theResponseForm: Form[MucrOptions] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[MucrOptions]])
    verify(page).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  private def theQueryUcr: Option[UcrBlock] = {
    val captor = ArgumentCaptor.forClass(classOf[Option[UcrBlock]])
    verify(page).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def theManageMucrChoice: Option[ManageMucrChoice] = {
    val captor = ArgumentCaptor.forClass(classOf[Option[ManageMucrChoice]])
    verify(page).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  "MucrOptionsController on displayPage" when {

    "GET displayPage is invoked without data in cache" should {

      "return 200 (OK) response" in {

        val result = controller().displayPage()(getRequest)
        status(result) mustBe OK
      }

      "pass empty form to view" in {

        controller().displayPage()(getRequest).futureValue
        theResponseForm.value mustBe empty
      }

      "pass empty queryUcr to view" in {

        controller().displayPage()(getRequest).futureValue
        theQueryUcr mustNot be(defined)
      }

      "pass empty ManageMucrChoice to view" in {

        controller().displayPage()(getRequest).futureValue
        theManageMucrChoice mustNot be(defined)
      }
    }

    "GET displayPage is invoked with data in cache" should {

      val cachedForm = MucrOptions("123")
      val cachedManageMucrChoice = ManageMucrChoice(AssociateAnotherUcrToThis)
      val cachedAnswers = AssociateUcrAnswers(parentMucr = Some(cachedForm), manageMucrChoice = Some(cachedManageMucrChoice))
      val queryUcr = UcrBlock(ucr = validDucr, ucrType = Ducr)

      "return 200 (OK) response" in {

        val result = controller(cachedAnswers, Some(queryUcr)).displayPage()(getRequest)

        status(result) mustBe OK
      }

      "pass form to view" in {

        controller(cachedAnswers, Some(queryUcr)).displayPage()(getRequest).futureValue

        theResponseForm.value mustBe defined
        theResponseForm.value.get mustBe cachedForm
      }

      "pass QueryUcr to view" in {

        controller(cachedAnswers, Some(queryUcr)).displayPage()(getRequest).futureValue

        theQueryUcr mustBe defined
        theQueryUcr.get mustBe queryUcr
      }

      "pass ManageMucrChoice to view" in {

        controller(cachedAnswers, Some(queryUcr)).displayPage()(getRequest).futureValue

        theManageMucrChoice mustBe defined
        theManageMucrChoice.get mustBe cachedManageMucrChoice
      }
    }
  }

  "MucrOptionsController on submit" when {

    "provided with correct form" should {

      "return 303 (SEE_OTHER) response" in {

        val correctForm = Json.toJson(MucrOptions("GB/12SD-123455ASD"))

        val result = controller().submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "redirect to Associate UCR Summary controller" in {

        val correctForm = Json.toJson(MucrOptions("GB/12SD-123455ASD"))

        val result = controller().submit()(postRequest(correctForm))

        redirectLocation(result) mustBe Some(controllers.summary.routes.AssociateUcrSummaryController.displayPage().url)
      }
    }

    "provided with incorrect form" should {

      "return 400 (BAD_REQUEST)" in {

        val result = controller().submit()(postRequest(JsString("")))
        status(result) mustBe BAD_REQUEST
      }

      "pass form with errors to view" in {

        val cachedManageMucrChoice = ManageMucrChoice(AssociateAnotherUcrToThis)
        val cachedAnswers = AssociateUcrAnswers(manageMucrChoice = Some(cachedManageMucrChoice))
        val incorrectForm = JsString("")

        controller(cachedAnswers).submit()(postRequest(incorrectForm)).futureValue

        theResponseForm.errors mustNot be(empty)
      }

      "pass ManageMucrChoice to view" in {

        val cachedManageMucrChoice = ManageMucrChoice(AssociateAnotherUcrToThis)
        val cachedAnswers = AssociateUcrAnswers(manageMucrChoice = Some(cachedManageMucrChoice))
        val incorrectForm = JsString("")

        controller(cachedAnswers).submit()(postRequest(incorrectForm)).futureValue

        theManageMucrChoice mustBe defined
        theManageMucrChoice mustBe cachedAnswers.manageMucrChoice
      }
    }
  }

}
