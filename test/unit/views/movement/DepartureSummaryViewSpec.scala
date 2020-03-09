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

package views.movement

import base.Injector
import models.cache.{ArrivalAnswers, DepartureAnswers}
import play.api.test.Helpers._
import views.ViewSpec
import views.html.summary.departure_summary_page

class DepartureSummaryViewSpec extends ViewSpec with Injector {

  private val answers = DepartureAnswers()

  private implicit val request = journeyRequest(answers)

  private val page = instanceOf[departure_summary_page]

  "View" should {

    "render title" in {

      page(answers).getTitle must containMessage("summary.departure.title")
    }

    "render heading" in {

      page(answers).getElementById("title") must containMessage("summary.departure.title")
    }

    "render back button" in {

      val backButton = page(answers).getGovUkBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.TransportController.displayPage())
    }

    "render sub-headers for summary sections" in {

      val summaryContent = contentAsString(page(answers))

      summaryContent must include(messages("summary.consignmentDetails"))
      summaryContent must include(messages("location.title"))
      summaryContent must include(messages("departureDetails.title"))
      summaryContent must include(messages("goodsDeparted.title"))
      summaryContent must include(messages("transport.title"))
    }
  }

}
