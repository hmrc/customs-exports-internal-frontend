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

package base

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.Consolidation
import models.notifications.NotificationFrontendModel
import models.requests.MovementRequest
import models.submissions.SubmissionFrontendModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

trait MockExportsMovementsConnector extends MockitoSugar with BeforeAndAfterEach { self: Suite =>

  val customsExportsMovementConnector: CustomsDeclareExportsMovementsConnector =
    mock[CustomsDeclareExportsMovementsConnector]

  def withConnectorSubmittingSuccessfully(): Unit = {
    when(customsExportsMovementConnector.submit(any[MovementRequest])(any())).thenReturn(Future.successful((): Unit))
    when(customsExportsMovementConnector.submit(any[Consolidation])(any())).thenReturn(Future.successful((): Unit))
  }

  def withConnectorFetchingNotifications(notificationsToReturn: Seq[NotificationFrontendModel]): Unit =
    when(customsExportsMovementConnector.fetchNotifications(any[String], any[String])(any())).thenReturn(Future.successful(notificationsToReturn))

  def withConnectorFetchingAllSubmissions(submissionsToReturn: Seq[SubmissionFrontendModel]): Unit =
    when(customsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(submissionsToReturn))

  def withConnectorFetchingSingleSubmission(submissionToReturn: Option[SubmissionFrontendModel]): Unit =
    when(customsExportsMovementConnector.fetchSingleSubmission(any[String], any[String])(any())).thenReturn(Future.successful(submissionToReturn))

  override protected def afterEach(): Unit = {
    reset(customsExportsMovementConnector)

    super.afterEach()
  }
}
