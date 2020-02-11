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
import models.notifications.queries.{MovementInfo, MucrInfo, UcrInfo}
import models.viewmodels.decoder.ROECode.UnknownRoe
import models.viewmodels.decoder.{ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.ile_query_mucr_response

class IleQueryMucrResponseViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_mucr_response]

  val arrival =
    MovementInfo(messageCode = "EAL", goodsLocation = "GBAUFXTFXTFXT", movementDateTime = Some(ZonedDateTime.parse("2019-10-23T12:34:18Z").toInstant))
  val retro =
    MovementInfo(messageCode = "RET", goodsLocation = "GBAUDFGFSHFKD", movementDateTime = Some(ZonedDateTime.parse("2019-11-01T02:34:18Z").toInstant))
  val depart = MovementInfo(
    messageCode = "EDL",
    goodsLocation = "GBAUFDSASFDFDF",
    movementDateTime = Some(ZonedDateTime.parse("2019-10-30T10:22:18Z").toInstant)
  )

  val status = EntryStatus(Some("ICS"), None, Some("SOE"))
  val mucrInfo =
    MucrInfo(ucr = "8GB123458302100-101SHIP1", movements = Seq.empty, entryStatus = Some(status), isShut = Some(true))

  private def view(info: MucrInfo = mucrInfo, parent: Option[MucrInfo] = None, associatedConsignments: Seq[UcrInfo] = Seq.empty) =
    page(info, parent, associatedConsignments)

  private def summaryElement(html: Html, index: Int) = html.getElementById("summary").select(s"div:eq($index)>dd").get(0)
  private def parentConsignmentElement(html: Html, index: Int) = html.getElementById("parentConsignment").select(s"div:eq($index)>dd").get(0)

  "Ile Query page" should {

    "render title" in {

      view().getTitle must containMessage("ileQueryResponse.ducr.title")
    }

    "render arrival movement" in {
      val arrivalView = view(mucrInfo.copy(movements = Seq(arrival)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.eal")
      arrivalView.getElementById("movement_date_0").text() must be("23 October 2019 at 13:34")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFXTFXTFXT")
    }

    "render departure movement" in {
      val arrivalView = view(mucrInfo.copy(movements = Seq(depart)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.edl")
      arrivalView.getElementById("movement_date_0").text() must be("30 October 2019 at 10:22")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFDSASFDFDF")
    }

    "render retrospective arrival" in {
      val arrivalView = view(mucrInfo.copy(movements = Seq(retro)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.ret")
      arrivalView.getElementById("movement_date_0").text() must be("1 November 2019 at 02:34")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUDFGFSHFKD")
    }

    "render movements by date order" in {
      val movementsView = view(mucrInfo.copy(movements = Seq(arrival, retro, depart)))
      movementsView.getElementById("movement_date_0").text() must be("1 November 2019 at 02:34")
      movementsView.getElementById("movement_date_1").text() must be("30 October 2019 at 10:22")
      movementsView.getElementById("movement_date_2").text() must be("23 October 2019 at 13:34")
    }

    "render no route of entry" in {
      summaryElement(view(), 0).text must be("")
    }

    "render unknown route of entry" in {
      summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(roe = Some(UnknownRoe))))), 0) must containMessage(
        "ileQueryResponse.route.unknown"
      )
    }

    "translate all routes of entry" in {
      ROECode.codes
        .filterNot(_ == UnknownRoe)
        .foreach(roe => summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(roe = Some(roe))))), 0) must containMessage(roe.messageKey))
    }

    "render default status of entry" in {
      summaryElement(view(), 1).text must be("")
    }

    "render empty status of entry" in {
      summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(soe = None)))), 1).text must be("")
    }

    "translate all status of entry" in {
      SOECode.MucrCodes.foreach(
        soe => summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(soe = Some(soe.code))))), 1) must containMessage(soe.messageKey)
      )
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
