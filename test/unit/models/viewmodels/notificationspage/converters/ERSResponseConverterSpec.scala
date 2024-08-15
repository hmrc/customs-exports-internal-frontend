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

package models.viewmodels.notificationspage.converters

import models.UcrBlock
import models.notifications.{Entry, EntryStatus, ResponseType}
import models.viewmodels.decoder.{Decoder, ICSCode, ROECode, SOECode}
import models.viewmodels.notificationspage.MovementTotalsResponseType.ERS
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import base.UnitSpec
import testdata.CommonTestData._
import testdata.NotificationTestData.exampleNotificationFrontendModel

import java.time.{Instant, LocalDate, ZoneOffset}

class ERSResponseConverterSpec extends UnitSpec with BeforeAndAfterEach {

  import ERSResponseConverterSpec._

  private implicit val messages: Messages = stubMessages()

  private val decoder: Decoder = mock[Decoder]
  private val contentBuilder = new ERSResponseConverter(decoder)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(decoder)
    when(decoder.ics(any[String])).thenReturn(Some(icsKeyFromDecoder))
    when(decoder.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoder.ducrSoe(any[String])).thenReturn(Some(soeKeyFromDecoder))
  }

  "ERSResponseConverter on convert" when {

    "provided with ERS MovementTotalsResponse with all codes" should {

      "call Decoder" in {
        val input = ersResponseAllCodes

        contentBuilder.convert(input)

        verify(decoder).ics(meq(icsKeyFromDecoder.code))
        verify(decoder).roe(meq(roeKeyFromDecoder.code))
        verify(decoder).ducrSoe(meq(soeKeyFromDecoder.code))
        verify(decoder, times(0)).crc(any())
        verify(decoder, times(0)).mucrSoe(any())
      }

      "return NotificationsPageSingleElement with values returned by Messages" in {
        val input = ersResponseAllCodes
        val expectedTitle = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContentElements = Seq(
          messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe"),
          roeKeyFromDecoder.code,
          messages(roeKeyFromDecoder.messageKey),
          messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe"),
          messages(soeKeyFromDecoder.messageKey),
          soeKeyFromDecoder.code,
          messages(icsKeyFromDecoder.messageKey),
          icsKeyFromDecoder.code
        )

        val result = contentBuilder.convert(input)

        result.title mustBe expectedTitle
        result.timestampInfo mustBe expectedTimestampInfo

        val contentAsString = result.content.toString
        expectedContentElements.map { contentElement =>
          contentAsString must include(contentElement)
        }
      }
    }

    "provided with ERS MovementTotalsResponse with empty codes" should {

      "call Decoder only for existing codes" in {
        val input = ersResponseMissingCodes

        contentBuilder.convert(input)

        verify(decoder, times(0)).ics(any())
        verify(decoder, times(0)).roe(any())
        verify(decoder).ducrSoe(meq(soeKeyFromDecoder.code))
      }

      "return NotificationsPageSingleElement without content for missing codes" in {
        val input = ersResponseMissingCodes
        val expectedTitle = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContentElements =
          Seq(messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe"), messages(soeKeyFromDecoder.messageKey))

        val result = contentBuilder.convert(input)

        result.title mustBe expectedTitle
        result.timestampInfo mustBe expectedTimestampInfo

        val contentAsString = result.content.toString
        expectedContentElements.map { contentElement =>
          contentAsString must include(contentElement)
        }
      }
    }

    "provided with ERS MovementTotalsResponse with unknown codes" should {

      "call Decoder for all codes" in {
        val input = ersResponseUnknownCodes

        contentBuilder.convert(input)

        verify(decoder).ics(meq(UnknownIcsCode))
        verify(decoder).roe(meq(UnknownRoeCode().code))
        verify(decoder).ducrSoe(meq(UnknownSoeCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in {
        when(decoder.ics(meq(UnknownIcsCode))).thenReturn(None)
        when(decoder.roe(meq(UnknownRoeCode().code))).thenReturn(None)
        when(decoder.ducrSoe(meq(UnknownSoeCode))).thenReturn(None)

        val input = ersResponseUnknownCodes
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

object ERSResponseConverterSpec {

  val testTimestamp: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  val icsKeyFromDecoder = ICSCode.InvalidationByCustoms
  val roeKeyFromDecoder = ROECode.DocumentaryControl
  val soeKeyFromDecoder = SOECode.DeclarationAcceptance

  val ersResponseAllCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
        entryStatus = Some(EntryStatus(ics = Some(icsKeyFromDecoder.code), roe = Some(roeKeyFromDecoder), soe = Some(soeKeyFromDecoder.code)))
      )
    )
  )

  val ersResponseMissingCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries =
      Seq(Entry(ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")), entryStatus = Some(EntryStatus(soe = Some(soeKeyFromDecoder.code)))))
  )

  val UnknownIcsCode = "123"
  val UnknownRoeCode = ROECode.UnknownRoe
  val UnknownSoeCode = "7890"

  val ersResponseUnknownCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
        entryStatus = Some(EntryStatus(ics = Some(UnknownIcsCode), roe = Some(UnknownRoeCode()), soe = Some(UnknownSoeCode)))
      )
    )
  )
}
