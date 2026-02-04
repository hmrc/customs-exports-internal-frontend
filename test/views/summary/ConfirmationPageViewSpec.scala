/*
 * Copyright 2024 HM Revenue & Customs
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

package views.summary

import base.Injector
import controllers.exchanges.JourneyRequest
import controllers.ileQuery.routes.IleQueryController
import forms.ConsignmentReferences
import models.UcrType
import models.UcrType.{Ducr, DucrPart, Mucr}
import models.cache.*
import models.cache.JourneyType.*
import models.summary.Confirmation
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.summary.confirmation_page

class ConfirmationPageViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  private val page = instanceOf[confirmation_page]
  private val dummyUcr = "dummyUcr"
  private val conversationId = "conversationId"
  private val call = IleQueryController.getConsignmentInformation(dummyUcr)

  private def consignmentRefs(ucrType: UcrType = Ducr) = ConsignmentReferences(ucrType.codeValue, dummyUcr)

  "ConfirmationPageView" should {
    for (journeyType <- List(ARRIVE, DEPART, RETROSPECTIVE_ARRIVE, ASSOCIATE_UCR, DISSOCIATE_UCR, SHUT_MUCR))
      s"provided with ${journeyType.toString} Journey Type" when {

        val view = page(Confirmation(journeyType, conversationId, Some(consignmentRefs()), None))

        "render title" in {
          view.getTitle must containMessage(s"confirmation.title.${journeyType.toString}")
        }

        "render panel with heading" in {
          Option(view.getElementsByClass("govuk-panel").first()) mustBe defined
          view.getElementsByTag("h1").first() must containMessage(s"confirmation.title.${journeyType.toString}")
        }

        "render body text with link to View Requests page" in {
          val bodyText = view.getElementsByClass("govuk-body").first()
          bodyText must containMessage("confirmation.bodyText", messages("confirmation.notification.timeline.link"))

          val link = bodyText.getElementsByClass("govuk-link").first()
          link must containMessage("confirmation.notification.timeline.link")
          link must haveHref(controllers.routes.ViewNotificationsController.listOfNotifications(conversationId))
        }

        "render sub-heading" in {
          val subHeading = view.getElementsByClass("govuk-heading-m").first()
          subHeading must containMessage("confirmation.subheading")
        }

        "render link to choice page" in {
          val link = view.getElementById("choice-link")

          link must containMessage("confirmation.redirect.choice.link")
          link must haveHref(controllers.routes.RootController.displayPage)
        }
      }

    for {
      ucrType <- List(Mucr, Ducr, DucrPart)
      answer <- List(ArrivalAnswers(), DepartureAnswers(), DisassociateUcrAnswers(), RetrospectiveArrivalAnswers())
    } s"provided with ${answer.`type`} Journey Type and $ucrType" should {
      implicit val request: JourneyRequest[?] = journeyRequest(answer)
      val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), None))

      "render summary list with one row" in {
        val summaryList = view.getElementsByClass("govuk-summary-list").first()
        summaryList must not be null

        val rows = summaryList.getElementsByClass("govuk-summary-list__row")
        rows.size mustBe 1

        val keyCell = rows.get(0).getElementsByClass("govuk-summary-list__key").first()
        val valueCell = rows.get(0).getElementsByClass("govuk-summary-list__value").first()

        ucrType match {
          case Ducr | DucrPart => keyCell must containMessage("confirmation.D")
          case Mucr            => keyCell must containMessage("confirmation.MUCR")
        }

        valueCell must containText(dummyUcr)
        valueCell.child(0) must haveHref(call)
      }
    }

    for {
      ucrType <- List(Mucr, Ducr, DucrPart)
      answer <- List(AssociateUcrAnswers())
    } s"provided with ${answer.`type`} Journey Type and $ucrType" should {
      implicit val request: JourneyRequest[?] = journeyRequest(answer)
      val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), Some(dummyUcr)))

      "render summary list with two rows" in {
        val summaryList = view.getElementsByClass("govuk-summary-list").first()
        summaryList must not be null

        val rows = summaryList.getElementsByClass("govuk-summary-list__row")
        rows.size mustBe 2

        val firstKey = rows.get(0).getElementsByClass("govuk-summary-list__key").first()
        val firstValue = rows.get(0).getElementsByClass("govuk-summary-list__value").first()

        ucrType match {
          case Ducr | DucrPart => firstKey must containMessage("confirmation.D")
          case Mucr            => firstKey must containMessage("confirmation.MUCR")
        }

        firstValue must containText(dummyUcr)
        firstValue.child(0) must haveHref(call)

        val secondKey = rows.get(1).getElementsByClass("govuk-summary-list__key").first()
        val secondValue = rows.get(1).getElementsByClass("govuk-summary-list__value").first()

        secondKey must containMessage("confirmation.MUCR")
        secondValue must containText(dummyUcr)
        secondValue.child(0) must haveHref(call)
      }
    }

    for {
      ucrType <- List(Mucr)
      answer <- List(ShutMucrAnswers())
    } s"provided with ${answer.`type`} Journey Type" should {
      implicit val request: JourneyRequest[?] = journeyRequest(answer)
      val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), None))

      "render summary list with one row" in {
        val summaryList = view.getElementsByClass("govuk-summary-list").first()
        summaryList must not be null

        val rows = summaryList.getElementsByClass("govuk-summary-list__row")
        rows.size mustBe 1

        val keyCell = rows.get(0).getElementsByClass("govuk-summary-list__key").first()
        val valueCell = rows.get(0).getElementsByClass("govuk-summary-list__value").first()

        keyCell must containMessage("confirmation.MUCR")
        valueCell must containText(dummyUcr)
        valueCell.child(0) must haveHref(call)
      }
    }
  }
}
