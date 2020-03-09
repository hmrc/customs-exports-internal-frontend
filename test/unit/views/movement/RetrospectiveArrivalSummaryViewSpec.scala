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
import models.cache.RetrospectiveArrivalAnswers
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import views.ViewSpec
import views.html.summary.retrospective_arrival_summary_page

class RetrospectiveArrivalSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(RetrospectiveArrivalAnswers())

  private val page = instanceOf[retrospective_arrival_summary_page]

  private val answers = RetrospectiveArrivalAnswers()

  "View" should {

    "render title" in {

      page(answers).getTitle must containMessage("summary.retrospectiveArrival.title")
    }

    "render heading" in {

      page(answers).getElementById("title") must containMessage("summary.retrospectiveArrival.title")
    }

    "render back button" in {

      val backButton = page(answers).getGovUkBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.movements.routes.LocationController.displayPage())
    }

    "render sub-headers for summary sections" in {

      val summaryContent = contentAsString(page(answers))

      summaryContent must include(messages("summary.consignmentDetails"))
      summaryContent must include(messages("location.title"))
    }
  }

}
