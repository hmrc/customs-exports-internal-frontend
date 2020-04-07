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
import models.{UcrBlock, UcrType}
import models.UcrBlock.mucrType
import models.cache._
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
    givenTheCacheIsEmpty
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

  "GET for specific journey" should {

    "return 303 (SEE_OTHER) and redirect to the correct controller" when {

      "user chooses arrival" in {

        val result = controller().startSpecificJourney(Choice.Arrival)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
      }

      "user chooses departure" in {

        val result = controller().startSpecificJourney(Choice.Departure)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
      }

      "user chooses associate ucr" in {

        val result = controller().startSpecificJourney(Choice.AssociateUCR)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
      }

      "user chooses dissociate ucr" in {

        val result = controller().startSpecificJourney(Choice.DisassociateUCR)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidations.routes.DisassociateUCRSummaryController.displayPage().url)
      }

      "user chooses shut mucr" in {

        val result = controller().startSpecificJourney(Choice.ShutMUCR)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidations.routes.ShutMucrSummaryController.displayPage().url)
      }

      "user chooses retrospective arrival" in {

        val result = controller().startSpecificJourney(Choice.RetrospectiveArrival)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.LocationController.displayPage().url)
      }

      "user chooses view submissions" in {

        val result = controller().startSpecificJourney(Choice.ViewSubmissions)(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ViewSubmissionsController.displayPage().url)
      }
    }
  }

  "POST" should {

    def postWithChoice(choice: Choice): Request[AnyContentAsJson] = postRequest(Json.obj("choice" -> choice.value))

    "return 303 (SEE_OTHER) when authenticated" when {

      "user chooses arrival" in {

        val result = controller().submit(postWithChoice(Arrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
        theCacheUpserted.answers mustBe Some(ArrivalAnswers(Answers.fakeEORI))
      }

      "user chooses departure" in {

        val result = controller().submit(postWithChoice(Departure))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.SpecificDateTimeController.displayPage().url)
        theCacheUpserted.answers mustBe Some(DepartureAnswers(Answers.fakeEORI))
      }

      "user chooses associate UCR" in {

        val result = controller().submit(postWithChoice(AssociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
        theCacheUpserted.answers mustBe Some(AssociateUcrAnswers())
      }

      "user chooses disassociate UCR" in {

        val result = controller().submit(postWithChoice(DisassociateUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUCRSummaryController.displayPage().url)
        theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers())
      }

      "user chooses shut MUCR" in {

        val result = controller().submit(postWithChoice(ShutMUCR))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidations.routes.ShutMucrSummaryController.displayPage().url)
        theCacheUpserted.answers mustBe Some(ShutMucrAnswers())
      }

      "user chooses retrospective arrival" in {

        val result = controller().submit(postWithChoice(RetrospectiveArrival))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(movements.routes.LocationController.displayPage().url)
        theCacheUpserted.answers mustBe Some(RetrospectiveArrivalAnswers())
      }

      "user chooses view submissions" in {

        val result = controller(SuccessfulAuth()).submit(postWithChoice(ViewSubmissions))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ViewSubmissionsController.displayPage().url)
      }
    }

    "return 400 when invalid" in {

      val result = controller().submit(postRequest)

      status(result) mustBe BAD_REQUEST
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).submit(postRequest)

      status(result) mustBe FORBIDDEN
    }
  }

  "POST Choice after ILE query result" should {

    def postWithChoice(choice: Choice): Request[AnyContentAsJson] = postRequest(Json.obj("choice" -> choice.value))
    val queryResult = UcrBlock("mucr", "M")

    "return 303 (SEE_OTHER) when authenticated" when {

      "user chooses arrival following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(Arrival))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(
          ArrivalAnswers(Answers.fakeEORI, consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr")))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses departure following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(Departure))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(
          DepartureAnswers(Answers.fakeEORI, consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr")))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses associate UCR following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(AssociateUCR))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(AssociateUcrAnswers(Answers.fakeEORI, associateUcr = Some(AssociateUcr(UcrType.Mucr, "mucr"))))
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses disassociate UCR following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(DisassociateUCR))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(
          DisassociateUcrAnswers(Answers.fakeEORI, ucr = Some(DisassociateUcr(UcrType.Mucr, None, Some("mucr"))))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses shut MUCR following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(ShutMUCR))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(ShutMucrAnswers(Answers.fakeEORI, shutMucr = Some(ShutMucr("mucr"))))
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

      "user chooses retrospective arrival following ILE query results" in {

        givenTheCacheContains(Cache(providerId, queryResult))

        val result = controller().submit(postWithChoice(RetrospectiveArrival))

        status(result) mustBe SEE_OTHER
        theCacheUpserted.answers mustBe Some(
          RetrospectiveArrivalAnswers(Answers.fakeEORI, consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, "mucr")))
        )
        theCacheUpserted.queryUcr mustBe Some(queryResult)
      }

    }

    "return 400 when invalid" in {

      val result = controller().submit(postRequest)

      status(result) mustBe BAD_REQUEST
    }

    "return 403 when unauthenticated" in {

      val result = controller(UnsuccessfulAuth).submit(postRequest)

      status(result) mustBe FORBIDDEN
    }
  }
}
