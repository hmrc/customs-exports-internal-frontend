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

import models.notifications.ResponseType
import models.viewmodels.decoder.{ActionCode, Decoder, ILEError}
import org.mockito.ArgumentMatchers.{anyString, eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import base.UnitSpec
import testdata.NotificationTestData
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ControlResponseBlockedConverterSpec extends UnitSpec with BeforeAndAfterEach {

  import ControlResponseBlockedConverterSpec._

  private implicit val messages: Messages = stubMessages()

  private val decoder: Decoder = mock[Decoder]
  private val converter = new ControlResponseBlockedConverter(decoder)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(decoder)
    when(decoder.error(anyString)).thenReturn(Some(ILEError("CODE", "Messages.Key")))
    when(decoder.error(meq(unknownErrorCode))).thenReturn(None)
  }

  "ControlResponseBlockedConverter on convert" should {

    "return NotificationsPageSingleElement with correct title" in {

      val input = BlockedControlResponseSingleError
      val expectedTitle =
        messages("notifications.elem.title.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed")

      val result = converter.convert(input)

      result.title mustBe expectedTitle
    }

    "return NotificationsPageSingleElement with correct timestampInfo" in {

      val input = BlockedControlResponseSingleError
      val expectedTimestampInfo = "23 October 2019 at 12:34pm"

      val result = converter.convert(input)

      result.timestampInfo mustBe expectedTimestampInfo
    }
  }

  "ControlResponseBlockedConverter on convert" when {

    "response contains single known error code" should {

      "call Decoder for Error once" in {

        val input = BlockedControlResponseSingleError

        converter.convert(input)

        verify(decoder).error(meq(input.errorCodes.head))
      }

      "return NotificationsPageSingleElement with correct content" in {

        val input = BlockedControlResponseSingleError
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed.singleError")
        val expectedErrorExplanation = messages("Messages.Key")

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        contentAsString must include(expectedErrorExplanation)
      }
    }

    "response contains single unknown error code" should {

      "call Decoder for Error once" in {
        val input = BlockedControlResponseSingleUnknownError

        converter.convert(input)

        verify(decoder).error(meq(input.errorCodes.head))
      }

      "return NotificationsPageSingleElement with correct content" in {
        val input = BlockedControlResponseSingleUnknownError
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed.singleError")
        val expectedErrorExplanation = unknownErrorCode

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        contentAsString must include(expectedErrorExplanation)
      }
    }

    "response contains multiple known errors" should {

      "call Decoder for every Error" in {

        val input = BlockedControlResponseMultipleErrors

        converter.convert(input)

        input.errorCodes.foreach { errorCode =>
          verify(decoder).error(meq(errorCode))
        }
      }

      "return NotificationsPageSingleElement with correct content" in {

        val input = BlockedControlResponseMultipleErrors
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed.multiError")
        val expectedErrorExplanations = List.fill(input.errorCodes.length)(messages("Messages.Key"))

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        expectedErrorExplanations.foreach { errorExplanation =>
          contentAsString must include(errorExplanation)
        }
      }
    }

    "response contains multiple known and unknown errors" should {

      "call Decoder for every Error" in {
        val input = BlockedControlResponseMultipleErrorsWithUnknown

        converter.convert(input)

        input.errorCodes.foreach { errorCode =>
          verify(decoder).error(meq(errorCode))
        }
      }

      "return NotificationsPageSingleElement with correct content" in {
        val input = BlockedControlResponseMultipleErrorsWithUnknown
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed.multiError")
        val expectedErrorExplanations = List.fill(input.errorCodes.length)(messages("Messages.Key"))

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        expectedErrorExplanations.foreach { errorExplanation =>
          contentAsString must include(errorExplanation)
        }
        contentAsString must include(BlockedControlResponseMultipleErrorsWithUnknown.errorCodes.last)
      }
    }
  }
}

object ControlResponseBlockedConverterSpec {
  val unknownErrorCode = "E533"

  private val BlockedControlResponse = exampleNotificationFrontendModel(
    responseType = ResponseType.ControlResponse,
    timestampReceived = NotificationTestData.testTimestamp,
    actionCode = Some(ActionCode.PartiallyAcknowledgedAndProcessed.code)
  )

  val BlockedControlResponseSingleError = BlockedControlResponse.copy(errorCodes = Seq("07"))

  val BlockedControlResponseSingleUnknownError = BlockedControlResponse.copy(errorCodes = Seq(unknownErrorCode))

  val BlockedControlResponseMultipleErrors = BlockedControlResponse.copy(errorCodes = Seq("07", "E3481", "29", "E607"))

  val BlockedControlResponseMultipleErrorsWithUnknown = BlockedControlResponse.copy(errorCodes = Seq("07", "E3481", "29", "E607", unknownErrorCode))
}
