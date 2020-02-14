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

package views

import base.Injector
import models.notifications.EntryStatus
import models.notifications.queries.{MovementInfo, MucrInfo, UcrInfo}
import models.viewmodels.decoder.{ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.ile_query_mucr_response

class IleQueryMucrResponseViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_mucr_response]

  val movement = MovementInfo("EAL", "goods")
  val mucrInfo = MucrInfo(ucr = "mucr", movements = Seq(movement), isShut = Some(true))
  val parentInfo = MucrInfo("parentUcr")
  val associatedInfo = MucrInfo("childUcr")

  private def view(info: MucrInfo = mucrInfo, parent: Option[MucrInfo] = None, associatedConsignments: Seq[UcrInfo] = Seq.empty) =
    page(info, parent, associatedConsignments)

  private def summaryElement(html: Html, index: Int) = html.getElementById("summary").select(s"div:eq($index)>dd").get(0)
  private def parentConsignmentElement(html: Html, index: Int) = html.getElementById("parentConsignment").select(s"div:eq($index)>dd").get(0)

  "Ile Query page" should {

    "render title" in {
      view().getTitle must containMessage("ileQueryResponse.ducr.title")
    }

    "render queried ucr summary" in {
      view().getElementById("summary") must containMessage("ileQueryResponse.details")
    }

    "render previous movements" in {
      view().getElementById("previousMovements") must containMessage("ileQueryResponse.previousMovements")
    }

    "render parent" in {
      view(parent = Some(parentInfo)).getElementById("parentConsignment") must containMessage("ileQueryResponse.parent")
    }

    "render associated consignments" in {
      view(associatedConsignments = Seq(associatedInfo)).getElementById("associatedUcrs") must containMessage("ileQueryResponse.associated")
    }

    "render isShut when mucr shut" in {
      view(mucrInfo.copy(isShut = Some(true))).getElementById("isShutMucr") must containMessage("ileQueryResponse.details.isShutMucr.true")
    }

    "render isShut when mucr not shut" in {
      view(mucrInfo.copy(isShut = Some(false))).getElementById("isShutMucr") must containMessage("ileQueryResponse.details.isShutMucr.false")
    }

    "not render isShut when missing" in {
      view(mucrInfo.copy(isShut = None)).getElementById("isShutMucr") must be(null)
    }

    "not render isShut when mucr has parent" in {
      view(info = mucrInfo.copy(isShut = Some(true)), parent = Some(MucrInfo("mucr"))).getElementById("isShutMucr") must be(null)
    }

    val viewWithParent = view(
      parent =
        Some(MucrInfo("parentUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.ConsolidationOpen.code)))))
    )

    "render parent consignment link" in {
      parentConsignmentElement(viewWithParent, 0).getElementsByClass("govuk-link").first() must haveHref(
        controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("parentUcr")
      )
      parentConsignmentElement(viewWithParent, 0) must containText("parentUcr")
    }

    "render parent consignment route" in {
      parentConsignmentElement(viewWithParent, 1) must containMessage(ROECode.DocumentaryControl.messageKey)
    }

    "render parent consignment status" in {
      parentConsignmentElement(viewWithParent, 2) must containMessage(SOECode.ConsolidationOpen.messageKey)
    }

    "not render associate consignments section if there aren't any " in {
      view().getElementById("associatedUcrs") must be(null)
    }

    "render associate consignments section" in {
      val viewWithChild = view(
        associatedConsignments =
          Seq(MucrInfo("childUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.Departed.code)))))
      )
      viewWithChild.getElementById("associatedUcrs") must containMessage("ileQueryResponse.associated")

      val elmChild = viewWithChild.getElementById("associateUcr_0_ucr")
      elmChild must containText("childUcr")
      elmChild.getElementsByClass("govuk-link").first() must haveHref(
        controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("childUcr")
      )

      viewWithChild.getElementById("associateUcr_0_roe") must containMessage(ROECode.DocumentaryControl.messageKey)

      viewWithChild.getElementById("associateUcr_0_soe") must containMessage(SOECode.Departed.messageKey)
    }
  }
}
