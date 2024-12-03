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

package views

import base.Injector
import connectors.exchanges.ActionType.{ConsolidationType, MovementType}
import models.submissions.Submission
import models.{UcrBlock, UcrType}
import org.jsoup.nodes.Document
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import views.helpers.ViewDates
import views.html.view_submissions

import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

class ViewSubmissionsViewSpec extends ViewSpec with Injector {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[view_submissions]

  private val dateTime: Instant =
    LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private def createView(submissions: Seq[Submission] = Seq.empty): Html =
    page(submissions)(request, messages)

  "Submissions page" should {
    val view = createView()

    "contain title" in {
      view.getTitle must containMessage("submissions.title")
    }

    "render the back button" in {
      view.checkBackButton
    }

    "contain header" in {
      view.getElementById("title") must containMessage("submissions.title")
    }

    "contain information paragraph" in {
      view.getElementsByClass("govuk-body-l").first() must containMessage("submissions.summary")
    }

    "contain correct table headers" in {
      view.selectFirst(".govuk-table__header.ucr") // must containMessage("submissions.ucr")
      view.selectFirst(".govuk-table__header.submission-type") must containMessage("submissions.submissionType")
      view.selectFirst(".govuk-table__header.date-of-request") must containMessage("submissions.dateOfRequest")
      view.selectFirst(".govuk-table__header.submission-action") must containMessage("submissions.submissionAction")
    }

    "contain correct submission data" in {
      val shutMucrSubmission = Submission(
        requestTimestamp = dateTime,
        eori = "",
        conversationId = conversationId,
        ucrBlocks = Seq(UcrBlock(ucr = validMucr, ucrType = "M")),
        actionType = ConsolidationType.ShutMucr
      )

      val arrivalSubmission = Submission(
        requestTimestamp = dateTime.plus(31, MINUTES),
        eori = "",
        conversationId = conversationId_2,
        ucrBlocks = Seq(UcrBlock(ucr = validDucr, ucrType = "D")),
        actionType = MovementType.Arrival
      )

      val departureSubmission = Submission(
        requestTimestamp = dateTime.plus(33, MINUTES),
        eori = "",
        conversationId = conversationId_3,
        ucrBlocks = Seq(UcrBlock(ucr = validWholeDucrParts, ucrType = UcrType.DucrPart.codeValue)),
        actionType = MovementType.Departure
      )

      val associateSubmission = Submission(
        requestTimestamp = dateTime.plus(30, MINUTES),
        eori = "",
        conversationId = conversationId_4,
        ucrBlocks = Seq(UcrBlock(ucr = validDucr, ucrType = "D"), UcrBlock(ucr = validMucr, ucrType = "M")),
        actionType = ConsolidationType.DucrAssociation
      )

      val pageWithData: Document = createView(Seq(shutMucrSubmission, arrivalSubmission, departureSubmission, associateSubmission))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")
      val secondDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(2)")
      val thirdDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(3)")
      val fourthDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(4)")

      firstDataRowElements.selectFirst(".ucr").text() mustBe s"$validMucr ${messages("submissions.hidden.text", validMucr)}"
      firstDataRowElements.selectFirst(".submission-type").text() mustBe "MUCR"
      firstDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(LocalDateTime.of(2019, 10, 31, 0, 0))
      firstDataRowElements.selectFirst(".submission-action") must containMessage("submissions.shutmucr")

      secondDataRowElements.selectFirst(".ucr").text() mustBe s"$validDucr ${messages("submissions.hidden.text", validDucr)}"
      secondDataRowElements.selectFirst(".submission-type").text() mustBe "DUCR"
      secondDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(LocalDateTime.of(2019, 10, 31, 0, 31))
      secondDataRowElements.selectFirst(".submission-action") must containMessage("submissions.arrival")

      thirdDataRowElements.selectFirst(".ucr").text() mustBe s"$validWholeDucrParts ${messages("submissions.hidden.text", validWholeDucrParts)}"
      thirdDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.DP")
      thirdDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(LocalDateTime.of(2019, 10, 31, 0, 33))
      thirdDataRowElements.selectFirst(".submission-action") must containMessage("submissions.departure")

      fourthDataRowElements
        .selectFirst(".ucr")
        .text() mustBe s"$validDucr $validMucr ${messages("submissions.hidden.text", validDucr + ", " + validMucr)}"
      fourthDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.D")
      fourthDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(LocalDateTime.of(2019, 10, 31, 0, 30))
      fourthDataRowElements.selectFirst(".submission-action") must containMessage("submissions.ducrassociation")
    }

    "contain MUCR and DUCR if Submission contains both" in {
      val pageWithData: Document = createView(Seq(exampleAssociateDucrRequestSubmission))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")

      val actualUcrs = firstDataRowElements.selectFirst(".ucr").text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = firstDataRowElements.selectFirst(".submission-type").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }
  }
}
