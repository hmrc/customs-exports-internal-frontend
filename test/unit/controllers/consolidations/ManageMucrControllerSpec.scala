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

package controllers.consolidations

import controllers.ControllerLayerSpec
import forms.ManageMucrChoice.{AssociateAnotherUcrToThis, AssociateThisToMucr}
import forms.{AssociateUcr, ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.UcrType._
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.CommonTestData.{validDucr, validMucr}
import views.html.associateucr.manage_mucr

import scala.concurrent.ExecutionContext.Implicits.global

class ManageMucrControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures {

  private val page = mock[manage_mucr]

  private val defaultUcrBlock = UcrBlock(ucr = validMucr, ucrType = Mucr)
  private def controller(answers: AssociateUcrAnswers, queryUcr: Option[UcrBlock] = Some(defaultUcrBlock)) =
    new ManageMucrController(SuccessfulAuth(), ValidJourney(answers, queryUcr), stubMessagesControllerComponents(), cacheRepository, page)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(page)
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  private def theFormPassedToView: Form[ManageMucrChoice] = {
    val captor: ArgumentCaptor[Form[ManageMucrChoice]] = ArgumentCaptor.forClass(classOf[Form[ManageMucrChoice]])
    verify(page).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def theQueryUcrPassedToView: Option[UcrBlock] = {
    val captor: ArgumentCaptor[Option[UcrBlock]] = ArgumentCaptor.forClass(classOf[Option[UcrBlock]])
    verify(page).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "ManageMucrController on displayPage" when {

    "GET displayPage is invoked without data in cache" should {

      "return 200 (OK) response" in {

        val result = controller(AssociateUcrAnswers()).displayPage()(getRequest)
        status(result)
      }

      "pass empty form to view" in {

        controller(AssociateUcrAnswers()).displayPage()(getRequest).futureValue
        theFormPassedToView.value mustBe empty
      }

      "pass queryUcr to view" in {

        controller(AssociateUcrAnswers()).displayPage()(getRequest).futureValue

        val queryUcr = theQueryUcrPassedToView
        queryUcr mustBe defined
        queryUcr.get mustBe UcrBlock(ucr = validMucr, ucrType = Mucr)
      }
    }

    "GET displayPage is invoked with data in cache" should {
      val cachedAnswers = AssociateUcrAnswers(manageMucrChoice = Some(ManageMucrChoice(AssociateAnotherUcrToThis)))

      "return 200 (OK) response" in {

        val result = controller(cachedAnswers).displayPage()(getRequest)
        status(result)
      }

      "pass form to view" in {

        controller(cachedAnswers).displayPage()(getRequest).futureValue

        val form = theFormPassedToView
        form.value mustBe defined
        form.value mustBe cachedAnswers.manageMucrChoice
      }

      "pass queryUcr to view" in {

        controller(cachedAnswers).displayPage()(getRequest).futureValue

        val queryUcr = theQueryUcrPassedToView
        queryUcr mustBe defined
        queryUcr.get mustBe UcrBlock(ucr = validMucr, ucrType = Mucr)
      }
    }

    "GET displayPage is invoked with Ducr queryUcr in cache" should {
      "throw IllegalStateException" in {

        val queryUcr = UcrBlock(ucr = validDucr, ucrType = Ducr)
        intercept[IllegalStateException] {
          await(controller(AssociateUcrAnswers(), Some(queryUcr)).displayPage()(getRequest))
        }
      }
    }
  }

  "ManageMucrController on submit" when {

    "provided with AssociateThisToMucr answer" should {

      "call CacheRepository passing Cache with queryUcr in childUcr field" in {

        val correctForm = Json.obj("choice" -> AssociateThisToMucr)

        controller(AssociateUcrAnswers()).submit()(postRequest(correctForm)).futureValue

        val expectedAnswers =
          AssociateUcrAnswers(childUcr = Some(AssociateUcr(defaultUcrBlock)), manageMucrChoice = Some(ManageMucrChoice(AssociateThisToMucr)))
        theCacheUpserted.answers mustBe Some(expectedAnswers)
      }

      "return 303 (SEE_OTHER) response" in {

        val correctForm = Json.obj("choice" -> AssociateThisToMucr)

        val result = controller(AssociateUcrAnswers()).submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "redirect to Mucr Options controller" in {

        val correctForm = Json.obj("choice" -> AssociateThisToMucr)

        val result = controller(AssociateUcrAnswers()).submit()(postRequest(correctForm))

        redirectLocation(result) mustBe Some(controllers.consolidations.routes.MucrOptionsController.displayPage().url)
      }
    }

    "provided with AssociateAnotherUcr answer" should {

      "call CacheRepository passing Cache with queryUcr in parentMucr field" in {

        val correctForm = Json.obj("choice" -> AssociateAnotherUcrToThis)

        controller(AssociateUcrAnswers()).submit()(postRequest(correctForm)).futureValue

        val expectedAnswers =
          AssociateUcrAnswers(parentMucr = Some(MucrOptions(defaultUcrBlock)), manageMucrChoice = Some(ManageMucrChoice(AssociateAnotherUcrToThis)))
        theCacheUpserted.answers mustBe Some(expectedAnswers)
      }

      "return 303 (SEE_OTHER) response" in {

        val correctForm = Json.obj("choice" -> AssociateAnotherUcrToThis)

        val result = controller(AssociateUcrAnswers()).submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "redirect to Associate UCR controller" in {

        val correctForm = Json.obj("choice" -> AssociateAnotherUcrToThis)

        val result = controller(AssociateUcrAnswers()).submit()(postRequest(correctForm))

        redirectLocation(result) mustBe Some(controllers.consolidations.routes.AssociateUcrController.displayPage().url)
      }
    }
  }

}
