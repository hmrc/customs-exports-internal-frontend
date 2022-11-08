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
import models.cache.JourneyType._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import views.html.confirmation_page

class ConfirmationPageViewSpec extends ViewSpec with MockitoSugar with Injector {

  private implicit val request = FakeRequest().withCSRFToken

  private val page = instanceOf[confirmation_page]
  private val dummyUcr = "dummyUcr"

  "ConfirmationPageView" should {
    for (journeyType <- List(ARRIVE, DEPART, RETROSPECTIVE_ARRIVE, ASSOCIATE_UCR, DISSOCIATE_UCR, SHUT_MUCR))
      s"provided with ${journeyType.toString} Journey Type" when {

        val view = page(journeyType, Some(dummyUcr))

        "render title" in {
          view.getTitle must containMessage(s"confirmation.title.${journeyType.toString}")
        }

        "render header" in {
          view
            .getElementsByClass("govuk-heading-xl")
            .first() must containMessage(s"confirmation.title.${journeyType.toString}")
        }

        "render inset text with link to View Requests page" in {

          val inset = view.getElementsByClass("govuk-inset-text").first()
          inset must containMessage("confirmation.insetText", messages("confirmation.notification.timeline.link"))

          val link = inset.getElementsByClass("govuk-link").first()
          link must containMessage("confirmation.notification.timeline.link")
          link must haveHref(controllers.routes.ViewSubmissionsController.displayPage())
        }

        "render sub-heading" in {
          val subHeading = view.getElementsByClass("govuk-heading-m").first()
          subHeading must containMessage("confirmation.subheading")
        }

        "render link to consignment information" in {
          val link = view.getElementById("summary-link")

          link must containMessage("confirmation.redirect.summary.link")
          link must haveHref(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation(dummyUcr))
        }

        "render link to choice page" in {
          val link = view.getElementById("choice-link")

          link must containMessage("confirmation.redirect.choice.link")
          link must haveHref(controllers.routes.ChoiceController.displayPage)
        }
      }

  }

}
