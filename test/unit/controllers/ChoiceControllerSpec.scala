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

package controllers

import controllers.actions.AuthenticatedAction
import controllers.consolidations.{routes => consolidationRoutes}
import forms.Choice._
import forms._
import models.UcrBlock.mucrType
import models.UcrType.{Ducr, Mucr}
import models.cache._
import models.{UcrBlock, UcrType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.CommonTestData.providerId
import views.html.choice_page

import scala.concurrent.ExecutionContext.Implicits.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val choicePage: choice_page = mock[choice_page]

  private def controller(auth: AuthenticatedAction = SuccessfulAuth()) =
    new ChoiceController(auth, stubMessagesControllerComponents(), cacheRepository, choicePage)

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(choicePage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def theResponseUcrBlock: Option[UcrBlock] = {
    val captor = ArgumentCaptor.forClass(classOf[Option[UcrBlock]])
    verify(choicePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenTheCacheIsEmpty()
    when(choicePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(choicePage)
    super.afterEach()
  }

  "GET" should {

    "redirect to query page" when {

      "an UCR is not available" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
      }
    }

    "return 200 when authenticated" when {

      "existing answers with no UcrBlock" in {

        givenTheCacheContains(Cache(providerId, Some(ArrivalAnswers()), None))

        val result = controller().displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value.get.value mustBe Arrival.value
        theResponseUcrBlock mustBe None
      }

      "existing answers with UcrBlock" in {

        val ucrBlock = UcrBlock("ucr", mucrType)
        givenTheCacheContains(Cache(providerId, None, Some(ucrBlock)))

        val result = controller().displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe None
        theResponseUcrBlock mustBe Some(ucrBlock)
      }
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).displayPage(getRequest)

      status(result) mustBe FORBIDDEN
    }
  }

  "POST" should {

    def postWithChoice(choice: Choice): Request[AnyContentAsJson] = postRequest(Json.obj("choice" -> choice.value))
    val queryResult = UcrBlock("mucr", Mucr.codeValue)

    "return 303 (SEE_OTHER) when authenticated" when {

      "user chooses arrival" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(Arrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
        theCacheUpserted.answers mustBe Some(ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr"))))
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses retrospective arrival" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(RetrospectiveArrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.LocationController.displayPage().url)
        theCacheUpserted.answers mustBe Some(
          RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr")))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses departure" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(Departure))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
        theCacheUpserted.answers mustBe Some(
          DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr")))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses associate UCR" when {

        "queryUcr is of type Ducr" in {

          val queryResultDucr = UcrBlock("ucr", Ducr.codeValue)
          givenTheCacheContains(Cache(providerId, None, Some(queryResultDucr)))

          val result = controller().submit(postWithChoice(AssociateUCR))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
          theCacheUpserted.answers mustBe Some(AssociateUcrAnswers(childUcr = Some(AssociateUcr(queryResultDucr))))
          theCacheUpserted.queryUcr mustBe Some(queryResultDucr)
        }

        "queryUcr is of type Mucr" in {

          givenTheCacheContains(Cache(providerId, None, Some(queryResult)))

          val result = controller().submit(postWithChoice(AssociateUCR))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.ManageMucrController.displayPage().url)
          theCacheUpserted.answers mustBe Some(AssociateUcrAnswers(childUcr = Some(AssociateUcr(queryResult))))
          theCacheUpserted.queryUcr mustBe Some(queryResult)
        }
      }

      "user chooses disassociate UCR" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(DisassociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUcrSummaryController.displayPage().url)
        theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers(ucr = Some(DisassociateUcr(UcrType.Mucr, None, Some("mucr")))))
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses shut MUCR" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(ShutMUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidations.routes.ShutMucrSummaryController.displayPage().url)
        theCacheUpserted.answers mustBe Some(ShutMucrAnswers(shutMucr = Some(ShutMucr("mucr"))))
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }
    }

    "return 400 when invalid" in {

      givenTheCacheContains(Cache(providerId, queryResult))

      val result = controller().submit(postRequest)

      status(result) mustBe BAD_REQUEST
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).submit(postRequest)

      status(result) mustBe FORBIDDEN
    }

    "redirect to query page" when {
      "queried UCR is not available" in {

        givenTheCacheIsEmpty()

        val result = controller().submit(postWithChoice(Arrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
      }
    }
  }

}
