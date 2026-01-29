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

package controllers.movements

import controllers.ControllerLayerSpec
import controllers.movements.routes.GoodsDepartedController
import controllers.summary.routes.ArriveDepartSummaryController
import forms.GoodsDeparted.DepartureLocation.OutOfTheUk
import forms.providers.TransportFormProvider
import forms.{ConsignmentReferenceType, ConsignmentReferences, GoodsDeparted, Transport}
import models.cache.{Answers, ArrivalAnswers, Cache, DepartureAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.test.Helpers.*
import play.twirl.api.HtmlFormat
import services.MockCache
import testdata.CommonTestData.providerId
import views.html.transport

import scala.concurrent.ExecutionContext.global

class TransportControllerSpec extends ControllerLayerSpec with MockCache {

  private val formProvider = mock[TransportFormProvider]
  private val page = mock[transport]
  private val consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, "referenceValue"))

  private def controller(answers: Answers) =
    new TransportController(SuccessfulAuth(), ValidJourney(answers), cacheRepository, formProvider, stubMessagesControllerComponents(), page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(formProvider.provideForm(any())).thenReturn(Transport.outOfTheUkForm)
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(formProvider)
    reset(page)

    super.afterEach()
  }

  private def theResponseForm: Form[Transport] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Transport]])
    verify(page).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def answersPassedToFormProvider: DepartureAnswers = {
    val captor = ArgumentCaptor.forClass(classOf[DepartureAnswers])
    verify(formProvider).provideForm(captor.capture())
    captor.getValue
  }

  "Location Controller on GET" should {

    "return 200 (OK)" when {

      "invoked without data for Transport in cache" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(OutOfTheUk)), consignmentReferences = consignmentReferences)
        whenTheCacheContains(Cache(providerId, Some(answers), None))

        val result = controller(answers).displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with data for Transport in cache" in {
        val cachedGoodsDeparted = Some(GoodsDeparted(OutOfTheUk))
        val cachedTransport = Some(Transport(Some("1"), Some("GB"), Some("123")))
        val answers =
          DepartureAnswers(goodsDeparted = cachedGoodsDeparted, transport = cachedTransport, consignmentReferences = consignmentReferences)
        whenTheCacheContains(Cache(providerId, Some(answers), None))

        val result = controller(answers).displayPage(getRequest)

        status(result) mustBe OK
        theResponseForm.value mustBe cachedTransport
      }
    }

    "return 303 (SEE_OTHER)" when {
      "there is no goods departed in the cache" in {
        whenTheCacheIsEmpty()

        val result = controller(DepartureAnswers(consignmentReferences = consignmentReferences)).displayPage(getRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe GoodsDepartedController.displayPage.url
      }
    }
  }

  "Location Controller on POST" when {

    "provided with incorrect form" should {
      "return 400 (BAD_REQUEST)" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(OutOfTheUk)), consignmentReferences = consignmentReferences)
        whenTheCacheContains(Cache(providerId, Some(answers), None))

        val invalidForm = List("modeOfTransport"->"99","nationality"->"Invalid","transportId"->"Invalid")

        val result = controller(answers).saveTransport()(postRequest(invalidForm: _*))

        status(result) mustBe BAD_REQUEST
      }
    }

    "user is on a different journey" should {
      "return 403 (FORBIDDEN)" in {
        whenTheCacheIsEmpty()

        val correctForm = List("modeOfTransport"->"1","nationality"->"GB","transportId"->"123")

        val result = controller(ArrivalAnswers()).saveTransport()(postRequest(correctForm: _*))

        status(result) mustBe FORBIDDEN
      }
    }

    "provided with correct form for departure" should {

      "call FormProvider passing answers with GoodsDeparted element" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(OutOfTheUk)))
        whenTheCacheContains(Cache(providerId, Some(answers), None))

        val correctForm = List("modeOfTransport"->"1","nationality"->"GB","transportId"->"123")

        await(controller(answers).saveTransport()(postRequest(correctForm: _*)))

        answersPassedToFormProvider mustBe answers
      }

      "return 303 (SEE_OTHER)" in {
        val answers = DepartureAnswers(goodsDeparted = Some(GoodsDeparted(OutOfTheUk)))
        whenTheCacheContains(Cache(providerId, Some(answers), None))

        val correctForm = List("modeOfTransport"->"1","nationality"->"GB","transportId"->"123")

        val result = controller(answers).saveTransport()(postRequest(correctForm: _*))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ArriveDepartSummaryController.displayPage.url)
      }
    }
  }
}
