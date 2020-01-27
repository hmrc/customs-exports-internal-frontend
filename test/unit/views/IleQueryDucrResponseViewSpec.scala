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

import models.notifications.EntryStatus
import models.notifications.queries.{DucrInfo, MovementInfo}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query_ducr_response

class IleQueryDucrResponseViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = new ile_query_ducr_response(main_template)

  val arrival =
    MovementInfo(messageCode = "EAL", goodsLocation = "GBAUFXTFXTFXT", movementDateTime = Some(ZonedDateTime.parse("2019-10-23T12:34:18Z").toInstant))
  val retro =
    MovementInfo(messageCode = "RET", goodsLocation = "GBAUDFGFSHFKD", movementDateTime = Some(ZonedDateTime.parse("2019-11-23T12:34:18Z").toInstant))
  val depart = MovementInfo(
    messageCode = "EDL",
    goodsLocation = "GBAUFDSASFDFDF",
    movementDateTime = Some(ZonedDateTime.parse("2019-10-30T12:34:18Z").toInstant)
  )

  val status = EntryStatus(Some("ICS"), Some("ROE"), Some("SOE"))
  val ducrInfo =
    DucrInfo(ucr = "8GB123458302100-101SHIP1", declarationId = "121332435432", movements = Seq.empty, entryStatus = Some(status))

  private def view(info: DucrInfo = ducrInfo) = page(info)

  "Ile Query page" should {

    "render title" in {

      view().getTitle must containMessage("ileQueryResponse.ducr.title")
    }

    "render arrival movement" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(arrival)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.eal")
      arrivalView.getElementById("movement_date_0").text() must be("23/10/2019")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFXTFXTFXT")
    }

    "render departure movement" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(depart)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.edl")
      arrivalView.getElementById("movement_date_0").text() must be("30/10/2019")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFDSASFDFDF")
    }

    "render retrospective arrival" in {
      val arrivalView = view(ducrInfo.copy(movements = Seq(retro)))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.ret")
      arrivalView.getElementById("movement_date_0").text() must be("23/11/2019")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUDFGFSHFKD")
    }

    "render movements by date order" in {
      val movementsView = view(ducrInfo.copy(movements = Seq(arrival, retro, depart)))
      movementsView.getElementById("movement_date_0").text() must be("23/11/2019")
      movementsView.getElementById("movement_date_1").text() must be("30/10/2019")
      movementsView.getElementById("movement_date_2").text() must be("23/10/2019")
    }

    "render default route of entry" in {
      view().getElementById("roe_code") must containMessage("ileQuery.mapping.roe.default", "ROE")
    }

    "render empty route of entry" in {
      view(ducrInfo.copy(entryStatus = Some(status.copy(roe = None))))
        .getElementById("roe_code")
        .text must be("")
    }

    "translate all routes of entry" in {
      IleCodeMapper.definedRoeCodes.foreach(
        code =>
          view(ducrInfo.copy(entryStatus = Some(status.copy(roe = Some(code)))))
            .getElementById("roe_code") must containMessage(s"ileQuery.mapping.roe.$code")
      )
    }

    "render default status of entry" in {
      view().getElementById("soe_code") must containMessage("ileQuery.mapping.soe.ducr.default", "SOE")
    }

    "render empty status of entry" in {
      view(ducrInfo.copy(entryStatus = Some(status.copy(soe = None))))
        .getElementById("soe_code")
        .text must be("")
    }

    "translate all status of entry" in {
      IleCodeMapper.definedSoeDucrCodes.foreach(
        code =>
          view(ducrInfo.copy(entryStatus = Some(status.copy(soe = Some(code)))))
            .getElementById("soe_code") must containMessage(s"ileQuery.mapping.soe.ducr.$code")
      )
    }

    "render default input customs status" in {
      view().getElementById("ics_code") must containMessage("ileQuery.mapping.ics.default", "ICS")
    }

    "render empty input customs status" in {
      view(ducrInfo.copy(entryStatus = Some(status.copy(ics = None))))
        .getElementById("ics_code")
        .text must be("")
    }

    "translate all input customs status" in {
      IleCodeMapper.definedIcsCodes.foreach(
        code =>
          view(ducrInfo.copy(entryStatus = Some(status.copy(ics = Some(code)))))
            .getElementById("ics_code") must containMessage(s"ileQuery.mapping.ics.$code")
      )
    }

  }
}
