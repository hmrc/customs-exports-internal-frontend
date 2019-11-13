/*
 * Copyright 2019 HM Revenue & Customs
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

package views.disassociate_ucr

import controllers.storage.FlashKeys
import play.api.mvc.{AnyContentAsEmpty, Flash, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociate_ucr_confirmation

class DisassociateUcrConfirmationViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val page = new disassociate_ucr_confirmation(main_template)

  "View" should {
    implicit val flash: Flash = Flash(Map(FlashKeys.UCR -> "ucr", FlashKeys.CONSOLIDATION_KIND -> "mucr"))
    val view = page()

    "render title" in {
      view.getTitle must containMessage("disassociate.ucr.confirmation.tab.heading", "mucr", "ucr")
    }

    "render confirmation dialogue" in {
      view.getElementById("highlight-box-heading") must containMessage("disassociate.ucr.confirmation.heading", "mucr", "ucr")
    }

    "have 'view requests' link" in {
      val statusInfo = page().getElementById("status-info")
      statusInfo.getElementsByTag("a").get(0) must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.ViewSubmissions))
    }

    "have 'next steps' link" in {
      val nextSteps = page().getElementById("next-steps")

      val associate = nextSteps.getElementsByTag("a").get(0)
      associate must containMessage("disassociation.confirmation.associateOrShut.associate")
      associate must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.AssociateUCR))

      val shut = nextSteps.getElementsByTag("a").get(1)
      shut must containMessage("disassociation.confirmation.associateOrShut.shut")
      shut must haveHref(controllers.routes.ChoiceController.startSpecificJourney(forms.Choice.ShutMUCR))
    }
  }

}
