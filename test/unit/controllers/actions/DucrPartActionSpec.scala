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

package controllers.actions

import base.UnitSpec
import config.DucrPartConfig
import controllers.exceptions.InvalidFeatureStateException
import controllers.exchanges.{AuthenticatedRequest, Operator}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContent, ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class DucrPartActionSpec extends UnitSpec with BeforeAndAfterEach {

  private val ducrPartConfig = mock[DucrPartConfig]
  private val ducrPartAction = new DucrPartAction(ducrPartConfig)(global)
  private val authenticatedRequest = new AuthenticatedRequest[AnyContent](Operator("12345"), FakeRequest())
  private val result = mock[Result]
  private val blockMethod: AuthenticatedRequest[AnyContent] => Future[Result] = _ => Future.successful(result)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(result.header).thenReturn(ResponseHeader(status = OK))
  }

  override protected def afterEach(): Unit = {
    reset(ducrPartConfig)

    super.afterEach()
  }

  "Ducr Part Action" should {

    "return result in block" when {

      "ducr part feature is enabled" in {

        when(ducrPartConfig.isDucrPartsEnabled).thenReturn(true)

        val result = ducrPartAction.invokeBlock(authenticatedRequest, blockMethod)

        status(result) mustBe OK
      }
    }

    "throw InvalidFeatureStateException" when {

      "ducr part feature is disabled" in {

        when(ducrPartConfig.isDucrPartsEnabled).thenReturn(false)

        intercept[InvalidFeatureStateException] {
          await(ducrPartAction.invokeBlock(authenticatedRequest, blockMethod))
        }
      }
    }
  }
}
