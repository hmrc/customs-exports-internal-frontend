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

package views.associateucr

import base.Injector
import forms.ManageMucrChoice._
import forms.{AssociateUcr, ManageMucrChoice, MucrOptions}
import models.UcrType.Ducr
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContent, Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec
import views.html.associateucr.associate_ucr_summary

class AssociateUcrSummaryViewSpec extends ViewSpec with Injector {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken
  private val page = instanceOf[associate_ucr_summary]

  private def createView(mucr: String, ducr: String, manageMucr: Option[ManageMucrChoice] = None): Html = {
    val associateUcrAnswers =
      AssociateUcrAnswers(manageMucrChoice = manageMucr, parentMucr = Some(MucrOptions(newMucr = mucr)), childUcr = Some(AssociateUcr(Ducr, ducr)))
    page(associateUcrAnswers)
  }

  "Associate UCR Confirmation View" should {

    val view = createView("MUCR", "DUCR")

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Reference' link on page" in {
      view.getElementsByClass("govuk-summary-list__value").first() must containText("DUCR")
    }

    "display mucr type on the page" in {

      view.getElementsByClass("govuk-summary-list__key").last() must containText(messages("associate.ucr.summary.kind.mucr"))
    }

    "render back button" when {

      def validateBackButton(backButton: Option[Element], call: Call): Unit = {
        backButton mustBe defined
        backButton.foreach { button =>
          button must containMessage("site.back")
          button must haveHref(call)
        }
      }

      "query Ducr" in {
        validateBackButton(view.getBackButton, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate this consignment to another" in {
        val view = createView("MUCR", "MUCR", Some(ManageMucrChoice(AssociateThisToMucr)))
        validateBackButton(view.getBackButton, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate another consignment to this one" in {
        val view = createView("MUCR", "MUCR", Some(ManageMucrChoice(AssociateAnotherUcrToThis)))
        validateBackButton(view.getBackButton, controllers.consolidations.routes.AssociateUcrController.displayPage())
      }
    }

    "render change link" when {

      def validateChangeLink(view: Html, call: Call): Unit = {
        val changeUcr = view.getElementsByClass("govuk-link").first()
        changeUcr must containMessage("site.change")
        changeUcr must haveHref(call)
      }

      "query Ducr" in {
        validateChangeLink(view, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate this consignment to another" in {
        val view = createView("MUCR", "MUCR", Some(ManageMucrChoice(AssociateThisToMucr)))
        validateChangeLink(view, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate another consignment to this one" in {
        val view = createView("MUCR", "MUCR", Some(ManageMucrChoice(AssociateAnotherUcrToThis)))
        validateChangeLink(view, controllers.consolidations.routes.AssociateUcrController.displayPage())
      }
    }
  }
}
