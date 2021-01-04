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

package views.components

import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.components.gds.siteHeader
import views.{ViewMatchers, ViewSpec}

class SiteHeaderViewSpec extends ViewSpec with ViewMatchers with MockitoSugar {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private def headerComponent = new siteHeader()

  "SiteHeader component" should {

    "render service name with link to 'find consignment' page" in {

      val serviceNameLink = headerComponent()
        .getElementsByClass("hmrc-internal-service-name__link")
        .first()
      serviceNameLink must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      serviceNameLink must containMessage("service.name")
    }

  }
}
