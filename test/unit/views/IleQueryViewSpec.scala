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

package views

import base.OverridableInjector
import config.DucrPartConfig
import forms.IleQueryForm
import org.jsoup.nodes.Element
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query

class IleQueryViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val ducrPartsConfig = mock[DucrPartConfig]
  private val injector = new OverridableInjector(bind[DucrPartConfig].to(ducrPartsConfig))

  private val page = injector.instanceOf[ile_query]
  private def view(form: Form[String] = IleQueryForm.form) = page(form)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ducrPartsConfig)
  }

  override protected def afterEach(): Unit =
    super.afterEach()

  "Ile Query page" should {

    "render title" in {
      when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
      view().getTitle must containMessage("ileQuery.title")
    }

    "render page header" in {
      when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
      view().getElementsByClass("govuk-label--xl").first().text() mustBe messages("ileQuery.title")
    }

    "render error summary" when {

      "no errors" in {
        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
        val govukErrorSummary: Element = view().getElementsByClass("govuk-error-summary__title").first()

        Option(govukErrorSummary) mustBe None
      }

      "some errors" in {
        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
        val errorView = page(IleQueryForm.form.withError("error", "error.required"))

        val govukErrorSummary = errorView.getElementsByClass("govuk-error-summary__title").first()

        govukErrorSummary.text() mustBe messages("error.summary.title")
      }
    }

    "contains input field" in {
      when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
      Option(view().getElementById("ucr")) mustBe defined
    }

    "contains submit button" in {
      when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
      view().getElementsByClass("govuk-button").first().text() mustBe messages("site.continue")
    }

    "contains link to view previous requests" in {
      when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
      val govukListElement = view().getElementsByClass("govuk-list").first()

      val previousRequests = govukListElement.getElementsByClass("govuk-link").get(0)

      previousRequests.text() mustBe messages("ileQuery.link.requests")
      previousRequests must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
    }

    "contain link to 'DUCR Part Details' page" when {

      "DucrPart feature is enabled" in {
        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(true)
        val govukListElement = view().getElementsByClass("govuk-list").first()

        val ducrPartDetailsLink = govukListElement.getElementsByClass("govuk-link").get(1)

        ducrPartDetailsLink must containMessage("ileQuery.link.ducrPart")
        ducrPartDetailsLink must haveHref(controllers.routes.DucrPartDetailsController.displayPage())
      }
    }

    "not contain link 'DUCR Part Details' page" when {

      "DucrPart feature is disabled" in {
        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)
        val govukListElement = view().getElementsByClass("govuk-list").first()

        govukListElement.getElementsByClass("govuk-link").size() mustBe 1
        val soleLink = govukListElement.getElementsByClass("govuk-link").get(0)
        soleLink must containMessage("ileQuery.link.requests")
      }
    }
  }
}
