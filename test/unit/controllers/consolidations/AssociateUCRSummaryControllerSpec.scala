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
import controllers.storage.FlashKeys
import forms.{AssociateUcr, MucrOptions}
import models.{ReturnToStartException, UcrType}
import models.cache.{Answers, AssociateUcrAnswers, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.associateucr.associate_ucr_summary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AssociateUCRSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val summaryPage = mock[associate_ucr_summary]
  private val submissionService = mock[SubmissionService]

  private def controller(answers: Answers) =
    new AssociateUCRSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, summaryPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, summaryPage)
    when(summaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, summaryPage)

    super.afterEach()
  }

  private def theResponseData: AssociateUcrAnswers = {
    val associateUcrAnswersCaptor = ArgumentCaptor.forClass(classOf[AssociateUcrAnswers])
    verify(summaryPage).apply(associateUcrAnswersCaptor.capture())(any(), any())
    associateUcrAnswersCaptor.getValue
  }

  private val mucrOptions = MucrOptions("MUCR")
  private val associateUcr = AssociateUcr(UcrType.Ducr, "DUCR")

  "Associate Ducr Summary Controller on displayPage" should {

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        val result = controller(AssociateUcrAnswers(parentMucr = Some(mucrOptions), childUcr = Some(associateUcr))).displayPage()(getRequest)

        status(result) mustBe OK
        verify(summaryPage).apply(any())(any(), any())

        val associateUcrAnswers = theResponseData
        associateUcrAnswers.childUcr mustBe defined
        associateUcrAnswers.childUcr.get.ucr mustBe "DUCR"
        associateUcrAnswers.parentMucr mustBe defined
        associateUcrAnswers.parentMucr.get.newMucr mustBe "MUCR"
      }
    }

    "throw an ReturnToStartException exception" when {

      "Mucr Options is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(parentMucr = None, childUcr = Some(associateUcr))).displayPage()(getRequest))
        } mustBe ReturnToStartException
      }

      "Associate Ducr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(parentMucr = Some(mucrOptions), childUcr = None)).displayPage()(getRequest))
        } mustBe ReturnToStartException
      }
    }
  }

  "Associate Ducr Summary Controller on submit" when {

    "everything works correctly" should {

      "call SubmissionService" in {

        val cachedAnswers = AssociateUcrAnswers(parentMucr = Some(mucrOptions), childUcr = Some(associateUcr))

        controller(cachedAnswers).submit()(postRequest(Json.obj())).futureValue

        val expectedProviderId = SuccessfulAuth().operator.providerId
        verify(submissionService).submit(meq(expectedProviderId), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to AssociateUcrConfirmation" in {

        val result =
          controller(AssociateUcrAnswers(parentMucr = Some(mucrOptions), childUcr = Some(associateUcr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.consolidations.routes.AssociateUCRConfirmationController.displayPage().url)
      }

      "return response with Movement Type in flash" in {

        val result =
          controller(AssociateUcrAnswers(parentMucr = Some(mucrOptions), childUcr = Some(associateUcr))).submit()(postRequest(Json.obj()))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ASSOCIATE_UCR.toString)
      }
    }
  }

}
