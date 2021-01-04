/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.associateucr.associate_ucr_confirmation

class AssociateUCRConfirmationViewSpec extends ViewSpec with Injector {

  private implicit val request = FakeRequest()
  private val page = instanceOf[associate_ucr_confirmation]

  "AssociateUCRConfirmationView" when {

    "View is rendered" should {

      "render title" in {

        page().getTitle must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "render header" in {

        page()
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "have 'notification timeline' link" in {
        val inset = page().getElementsByClass("govuk-inset-text").first()
        inset
          .getElementsByClass("govuk-link")
          .first() must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
      }

      "have 'find another consignment' link" in {
        page()
          .getElementsByClass("govuk-link")
          .get(1) must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      }
    }
  }

}
