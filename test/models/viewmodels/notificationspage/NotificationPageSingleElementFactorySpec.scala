/*
 * Copyright 2019 HM Revenue & Customs
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

package models.viewmodels.notificationspage

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import base.{MessagesStub, UnitSpec}
import com.google.inject.Guice
import models.UcrBlock
import models.notifications.NotificationFrontendModel
import models.submissions.{ActionType, SubmissionFrontendModel}
import models.viewmodels.notificationspage.converters._
import modules.DateTimeModule
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel

class NotificationPageSingleElementFactorySpec extends UnitSpec with MessagesStub with BeforeAndAfterEach {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private implicit val fakeRequest = FakeRequest()

  private val responseConverterProvider = mock[ResponseConverterProvider]
  private val factory = new NotificationPageSingleElementFactory(responseConverterProvider)

  private val injector = Guice.createInjector(new DateTimeModule())
  private val unknownResponseConverter = injector.getInstance(classOf[UnknownResponseConverter])

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(responseConverterProvider)
    when(responseConverterProvider.provideResponseConverter(any[NotificationFrontendModel]))
      .thenReturn(unknownResponseConverter)
  }

  "NotificationPageSingleElementFactory" should {

    "return NotificationsPageSingleElement with values returned by Messages" when {

      "provided with Arrival SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Arrival, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Arrival"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.Arrival", "DUCR")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with Departure SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Departure, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Departure"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.Departure", "DUCR")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with DucrAssociation SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel = SubmissionFrontendModel(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.DucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks =
            Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "D"), UcrBlock(ucr = correctUcr_3, ucrType = "D"))
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrAssociation"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrAssociation")}</p>" +
              s"<p>$correctUcr_2</p>" +
              s"<p>$correctUcr_3</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with MucrAssociation SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel = SubmissionFrontendModel(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.MucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "M"))
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.MucrAssociation"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.MucrAssociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with DucrDisassociation SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.DucrDisassociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "D"))
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrDisassociation"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrDisassociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with MucrDisassociation SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.MucrDisassociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"))
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.MucrDisassociation"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.MucrDisassociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with ShutMucr SubmissionFrontendModel" in {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(
            actionType = ActionType.ShutMucr,
            requestTimestamp = testTimestamp,
            ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"))
          )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.ShutMucr"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.ShutMucr")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }
  }

  "NotificationPageSingleElementFactory" when {

    "provided with NotificationFrontendModel" should {

      "call ResponseConverterProvider" in {

        val input = exampleNotificationFrontendModel()

        factory.build(input)

        verify(responseConverterProvider).provideResponseConverter(meq(input))
      }

      "call converter returned by ResponseConverterProvider" in {

        val exampleNotificationPageElement =
          NotificationsPageSingleElement(title = "TITLE", timestampInfo = "TIMESTAMP", content = Html("<test>HTML</test>"))
        val responseConverter = mock[NotificationPageSingleElementConverter]
        when(responseConverter.convert(any[NotificationFrontendModel])(any()))
          .thenReturn(exampleNotificationPageElement)

        when(responseConverterProvider.provideResponseConverter(any[NotificationFrontendModel]))
          .thenReturn(responseConverter)

        val input = exampleNotificationFrontendModel()

        factory.build(input)

        verify(responseConverter).convert(meq(input))(any[Messages])
      }

      "return NotificationsPageSingleElement returned by converter" in {

        val exampleNotificationPageElement =
          NotificationsPageSingleElement(title = "TITLE", timestampInfo = "TIMESTAMP", content = Html("<test>HTML</test>"))
        val responseConverter = mock[NotificationPageSingleElementConverter]
        when(responseConverter.convert(any[NotificationFrontendModel])(any()))
          .thenReturn(exampleNotificationPageElement)

        when(responseConverterProvider.provideResponseConverter(any[NotificationFrontendModel]))
          .thenReturn(responseConverter)

        val input = exampleNotificationFrontendModel()

        val result = factory.build(input)

        result mustBe exampleNotificationPageElement
      }
    }
  }

  private def assertEquality(actual: NotificationsPageSingleElement, expected: NotificationsPageSingleElement): Unit = {
    actual.title must equal(expected.title)
    actual.timestampInfo must equal(expected.timestampInfo)
    actual.content must equal(expected.content)
  }

}
