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

import base.Injector
import models.cache.JourneyType
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import views.html.confirmation_page

class ConfirmationPageViewSpec extends ViewSpec with MockitoSugar with Injector {

  private implicit val request = FakeRequest().withCSRFToken

  private val page = instanceOf[confirmation_page]

  "ConfirmationPageView" should {

    "render title" when {

      "provided with ARRIVE Journey Type" in {

        page(JourneyType.ARRIVE).getTitle must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        page(JourneyType.DEPART).getTitle must containMessage("confirmation.title.DEPART")
      }

      "provided with RETROSPECTIVE_ARRIVE Journey Type" in {

        page(JourneyType.RETROSPECTIVE_ARRIVE).getTitle must containMessage("confirmation.title.RETROSPECTIVE_ARRIVE")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        page(JourneyType.ASSOCIATE_UCR).getTitle must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        page(JourneyType.DISSOCIATE_UCR).getTitle must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        page(JourneyType.SHUT_MUCR).getTitle must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render header" when {

      "provided with ARRIVE Journey Type" in {

        page(JourneyType.ARRIVE)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        page(JourneyType.DEPART)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DEPART")
      }

      "provided with RETROSPECTIVE_ARRIVE Journey Type" in {

        page(JourneyType.RETROSPECTIVE_ARRIVE)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.RETROSPECTIVE_ARRIVE")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        page(JourneyType.ASSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        page(JourneyType.DISSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        page(JourneyType.SHUT_MUCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render inset text with link to View Requests page" in {

      val inset = page(JourneyType.ARRIVE).getElementsByClass("govuk-inset-text").first()
      inset must containMessage("confirmation.insetText")

      val link = inset.getElementsByClass("govuk-link").first()
      link must containMessage("confirmation.notification.timeline.link")
      link must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
    }

    "render 'Find another consignment' link to Find Consignment page" in {

      val link = page(JourneyType.ARRIVE).getElementsByClass("govuk-link").get(1)

      link must containMessage("confirmation.redirect.query.link")
      link must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
    }
  }

}
