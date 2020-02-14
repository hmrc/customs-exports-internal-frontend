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
import models.notifications.queries.{DucrInfo, MovementInfo, MucrInfo}
import models.viewmodels.decoder.{ICSCode, ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.ile_query_ducr_response

class IleQueryDucrResponseViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_ducr_response]

  val status = EntryStatus(Some("ICS"), None, Some("SOE"))
  val movement = MovementInfo("EAL", "goods")
  val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "id", movements = Seq(movement), entryStatus = Some(status))
  val parentInfo = MucrInfo("parentUcr")

  private def view(info: DucrInfo = ducrInfo, parent: Option[MucrInfo] = None) = page(info, parent)

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

    "render default status of entry" in {
      summaryElement(view(), 1).text must be("")
    }

    "render empty status of entry" in {
      summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(soe = None)))), 1).text must be("")
    }

    "translate all ducr status of entry" in {
      SOECode.DucrCodes.foreach(
        soe => summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(soe = Some(soe.code))))), 1) must containMessage(soe.messageKey)
      )
    }

    "render default input customs status" in {
      summaryElement(view(), 2).text must be("")
    }

    "render empty input customs status" in {
      summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(ics = None)))), 2).text must be("")
    }

    "translate all input customs status" in {
      ICSCode.codes.foreach(
        ics => summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(ics = Some(ics.code))))), 2) must containMessage(ics.messageKey)
      )
    }

    val viewWithParent = view(
      parent =
        Some(MucrInfo("parentUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.ConsolidationOpen.code)))))
    )

    "render parent consignment link" in {
      parentConsignmentElement(viewWithParent, 0).getElementsByClass("govuk-link").first() must haveHref(
        controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("parentUcr")
      )
      parentConsignmentElement(viewWithParent, 0).text() must be("parentUcr")
    }

    "render parent consignment route" in {
      parentConsignmentElement(viewWithParent, 1) must containMessage(ROECode.DocumentaryControl.messageKey)
    }

    "render parent consignment status" in {
      parentConsignmentElement(viewWithParent, 2) must containMessage(SOECode.ConsolidationOpen.messageKey)
    }
  }
}
