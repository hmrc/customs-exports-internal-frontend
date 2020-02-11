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

import java.time.ZonedDateTime

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

  val arrival =
    MovementInfo(messageCode = "EAL", goodsLocation = "GBAUFXTFXTFXT", movementDateTime = Some(ZonedDateTime.parse("2019-10-23T12:34:18Z").toInstant))
  val retro =
    MovementInfo(messageCode = "RET", goodsLocation = "GBAUDFGFSHFKD", movementDateTime = Some(ZonedDateTime.parse("2019-11-04T16:27:18Z").toInstant))
  val depart = MovementInfo(
    messageCode = "EDL",
    goodsLocation = "GBAUFDSASFDFDF",
    movementDateTime = Some(ZonedDateTime.parse("2019-10-30T09:17:18Z").toInstant)
  )

  val status = EntryStatus(Some("ICS"), Some(ROECode.DocumentaryControl), Some("SOE"))
  val ducrInfo =
    DucrInfo(ucr = "8GB123458302100-101SHIP1", declarationId = "121332435432", movements = Seq.empty, entryStatus = Some(status))

  private def view(info: DucrInfo = ducrInfo, parent: Option[MucrInfo] = None) = page(info, parent)

  private def summaryElement(html: Html, index: Int) = html.getElementById("summary").select(s"div:eq($index)>dd").get(0)
  private def parentConsignmentElement(html: Html, index: Int) = html.getElementById("parentConsignment").select(s"div:eq($index)>dd").get(0)

  "Ile Query page" should {

    "render title" in {

      view().getTitle must containMessage("ileQueryResponse.ducr.title")
    }

    "render arrival movement" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(arrival)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.eal")
      arrivalView.getElementById("movement_date_0").text() must be("23 October 2019 at 13:34")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFXTFXTFXT")
    }

    "render departure movement" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(depart)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.edl")
      arrivalView.getElementById("movement_date_0").text() must be("30 October 2019 at 09:17")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFDSASFDFDF")
    }

    "render retrospective arrival" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(retro)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.ret")
      arrivalView.getElementById("movement_date_0").text() must be("4 November 2019 at 16:27")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUDFGFSHFKD")
    }

    "render movements by date order" in {
      val movementsView = view(ducrInfo.copy(movements = Seq(arrival, retro, depart)))
      movementsView.getElementById("movement_date_0").text() must be("4 November 2019 at 16:27")
      movementsView.getElementById("movement_date_1").text() must be("30 October 2019 at 09:17")
      movementsView.getElementById("movement_date_2").text() must be("23 October 2019 at 13:34")
    }

    "render default route of entry" in {
      summaryElement(view(), 0).text must be("")
    }

    "render empty route of entry" in {
      summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(roe = None)))), 0).text must be("")
    }

    "translate all routes of entry" in {
      ROECode.codes.foreach(
        roe => summaryElement(view(ducrInfo.copy(entryStatus = Some(status.copy(roe = Some(roe))))), 0) must containMessage(roe.messageKey)
      )
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
      parent = Some(
        MucrInfo("parentUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.ConsolidationOpen.code))))
      )
    )

    "render parent consignment link" in {
      parentConsignmentElement(viewWithParent, 0).getElementsByClass("govuk-link").first() must haveHref(
        controllers.ileQuery.routes.IleQueryController.submitQuery("parentUcr")
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
