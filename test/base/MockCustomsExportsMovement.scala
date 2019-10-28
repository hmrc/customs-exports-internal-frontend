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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{ACCEPTED, BAD_REQUEST}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

trait MockCustomsExportsMovement extends MockitoSugar with BeforeAndAfterEach { self: Suite =>
  val mockCustomsExportsMovementConnector: CustomsDeclareExportsMovementsConnector =
    mock[CustomsDeclareExportsMovementsConnector]

  def sendMovementRequest202Response(): OngoingStubbing[Future[HttpResponse]] =
    when(
      mockCustomsExportsMovementConnector
        .sendArrivalDeclaration(any())(any())
    ).thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  def sendMovementRequest400Response(): OngoingStubbing[Future[HttpResponse]] =
    when(
      mockCustomsExportsMovementConnector
        .sendArrivalDeclaration(any())(any())
    ).thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

  override protected def afterEach(): Unit = {
    reset(mockCustomsExportsMovementConnector)

    super.afterEach()
  }
}