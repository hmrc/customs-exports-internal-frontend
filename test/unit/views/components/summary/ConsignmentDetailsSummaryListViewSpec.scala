/*
 * Copyright 2023 HM Revenue & Customs
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

package views.components.summary

import forms.{ConsignmentReferenceType, ConsignmentReferences}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import base.Injector
import testdata.CommonTestData.{validDucr, validMucr}
import views.html.components.summary.consignment_details_summary_list
import views.{ViewMatchers, ViewSpec}

class ConsignmentDetailsSummaryListViewSpec extends ViewSpec with ViewMatchers with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val consignmentDetailsList = instanceOf[consignment_details_summary_list]

  private val Ducr = ConsignmentReferences(ConsignmentReferenceType.D, validDucr)
  private val Mucr = ConsignmentReferences(ConsignmentReferenceType.M, validMucr)

  "ConsignmentDetailsSummaryList" should {

    "have heading" in {
      consignmentDetailsList(Some(Ducr)).getElementsByClass("govuk-heading-m").first() must containMessage("summary.consignmentDetails")
    }
  }

  "ConsignmentDetailsSummaryList" when {

    "provided with DUCR" should {

      "have Consignment Type row" in {
        val consignmentTypeRow = consignmentDetailsList(Some(Ducr)).getElementsByClass("govuk-summary-list__row").get(0)

        consignmentTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.referenceType")
        consignmentTypeRow.getElementsByClass("govuk-summary-list__value").first() must containMessage("consignmentReferences.reference.ducr")
      }

      "have Consignment Reference row" in {
        val consignmentTypeRow = consignmentDetailsList(Some(Ducr)).getElementsByClass("govuk-summary-list__row").get(1)

        consignmentTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.referenceValue")
        consignmentTypeRow.getElementsByClass("govuk-summary-list__value").first().text mustBe Ducr.referenceValue
      }
    }

    "provided with MUCR" should {

      "have Consignment Type row" in {
        val consignmentTypeRow = consignmentDetailsList(Some(Mucr)).getElementsByClass("govuk-summary-list__row").get(0)

        consignmentTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.referenceType")
        consignmentTypeRow.getElementsByClass("govuk-summary-list__value").first() must containMessage("consignmentReferences.reference.mucr")
      }

      "have Consignment Reference row" in {
        val consignmentTypeRow = consignmentDetailsList(Some(Mucr)).getElementsByClass("govuk-summary-list__row").get(1)

        consignmentTypeRow.getElementsByClass("govuk-summary-list__key").first() must containMessage("summary.referenceValue")
        consignmentTypeRow.getElementsByClass("govuk-summary-list__value").first().text mustBe Mucr.referenceValue
      }
    }
  }
}
