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

package views.consolidations

import controllers.routes
import models.cache.ShutMucrAnswers
import views.ViewSpec
import views.html.shut_mucr_confirmation

class ShutMucrConfirmationViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val page = new shut_mucr_confirmation(main_template)
  private val mucr = "some-mucr"

  "View" should {

    "display page reference" in {

      page(mucr).getElementById("highlight-box-heading") must containMessage("shutMucr.confirmation.heading", mucr)
    }

    "have what next section" in {

      page(mucr).getElementById("what-next") must containMessage("movement.confirmation.whatNext")
    }

    "display 'Back to start page' button on page" in {

      val backButton = page(mucr).getElementsByClass("button").first()

      backButton must containMessage("site.backToStart")
      backButton must haveHref(routes.ChoiceController.displayPage())
    }

  }

}
