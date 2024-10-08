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

package models.viewmodels.notificationspage.converters

import models.UcrBlock
import models.notifications.{Entry, EntryStatus, ResponseType}
import models.viewmodels.decoder.{CRCCode, Decoder, ROECode, SOECode}
import models.viewmodels.notificationspage.MovementTotalsResponseType.EMR
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import base.UnitSpec
import testdata.CommonTestData.correctUcr
import testdata.NotificationTestData.exampleNotificationFrontendModel

import java.time.{Instant, LocalDate, ZoneOffset}

class EMRResponseConverterSpec extends UnitSpec with BeforeAndAfterEach {

  import EMRResponseConverterSpec._

  private implicit val messages: Messages = stubMessages()

  private val decoder: Decoder = mock[Decoder]
  private val contentBuilder = new EMRResponseConverter(decoder)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(decoder)
    when(decoder.crc(any[String])).thenReturn(Some(crcKeyFromDecoder))
    when(decoder.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoder.mucrSoe(any[String])).thenReturn(Some(mucrSoeKeyFromDecoder))
  }

  "EMRResponseConverter on convert" when {

    "provided with EMR MovementTotalsResponse with all codes" should {

      "call Decoder" in {
        val input = emrResponseAllCodes

        contentBuilder.convert(input)

        verify(decoder).crc(meq(crcKeyFromDecoder.code))
        verify(decoder).roe(meq(roeKeyFromDecoder.code))
        verify(decoder).mucrSoe(meq(mucrSoeKeyFromDecoder.code))
        verify(decoder, times(0)).ics(any())
        verify(decoder, times(0)).ducrSoe(any())
      }

      "return NotificationsPageSingleElement with values returned by Messages" in {
        val input = emrResponseAllCodes
        val expectedTitle = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContentElements = Seq(
          messages(crcKeyFromDecoder.messageKey),
          crcKeyFromDecoder.code,
          messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe"),
          messages(roeKeyFromDecoder.messageKey),
          roeKeyFromDecoder.code,
          messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe"),
          messages(mucrSoeKeyFromDecoder.messageKey),
          mucrSoeKeyFromDecoder.code
        )

        val result = contentBuilder.convert(input)

        result.title mustBe expectedTitle
        result.timestampInfo mustBe expectedTimestampInfo

        val contentAsString = result.content.toString()
        expectedContentElements.map { contentElement =>
          contentAsString must include(contentElement)
        }
      }
    }

    "provided with EMR MovementTotalsResponse with empty codes" should {

      "call Decoder only for existing codes" in {
        val input = emrResponseMissingCodes

        contentBuilder.convert(input)

        verify(decoder).roe(meq(roeKeyFromDecoder.code))
        verify(decoder, times(0)).crc(any())
        verify(decoder, times(0)).mucrSoe(any())
      }

      "return NotificationsPageSingleElement without content for missing codes" in {
        val input = emrResponseMissingCodes
        val expectedTitle = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContentElements =
          Seq(messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe"), messages(roeKeyFromDecoder.messageKey))

        val result = contentBuilder.convert(input)

        result.title mustBe expectedTitle
        result.timestampInfo mustBe expectedTimestampInfo

        val contentAsString = result.content.toString()
        expectedContentElements.map { contentElement =>
          contentAsString must include(contentElement)
        }
      }
    }

    "provided with EMR MovementTotalsResponse with unknown codes" should {

      "call Decoder for all codes" in {
        val input = emrResponseUnknownCodes

        contentBuilder.convert(input)

        verify(decoder).crc(meq(UnknownCrcCode))
        verify(decoder).roe(meq(UnknownRoeCode().code))
        verify(decoder).mucrSoe(meq(UnknownMucrSoeCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in {
        when(decoder.crc(meq(UnknownCrcCode))).thenReturn(None)
        when(decoder.roe(meq(UnknownRoeCode().code))).thenReturn(None)
        when(decoder.mucrSoe(meq(UnknownMucrSoeCode))).thenReturn(None)

        val input = emrResponseUnknownCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = "31 October 2019 at 12:00am",
          content = Html("")
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }
}

object EMRResponseConverterSpec {

  val testTimestamp: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  val crcKeyFromDecoder = CRCCode.Success
  val roeKeyFromDecoder = ROECode.DocumentaryControl
  val mucrSoeKeyFromDecoder = SOECode.ConsolidationOpen

  val emrResponseAllCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    crcCode = Some(crcKeyFromDecoder.code),
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder), soe = Some(mucrSoeKeyFromDecoder.code)))
      )
    )
  )

  val emrResponseMissingCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")), entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder)))))
  )

  val UnknownCrcCode = "1234"
  val UnknownRoeCode = ROECode.UnknownRoe
  val UnknownMucrSoeCode = "7890"

  val emrResponseUnknownCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    crcCode = Some(UnknownCrcCode),
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = Some(UnknownRoeCode()), soe = Some(UnknownMucrSoeCode)))
      )
    )
  )
}
