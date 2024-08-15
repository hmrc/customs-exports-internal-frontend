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

package models.submissions

import connectors.exchanges.ActionType.MovementType
import models.UcrBlock
import org.scalatest.OptionValues
import base.UnitSpec
import testdata.CommonTestData._

class SubmissionSpec extends UnitSpec with OptionValues {

  def submission(ucrBlocks: UcrBlock*) =
    Submission(
      eori = validEori,
      conversationId = conversationId,
      ucrBlocks = Seq(ucrBlocks: _*),
      actionType = MovementType.Arrival,
      requestTimestamp = models.now
    )

  private val mucrBlock = UcrBlock(ucr = validMucr, ucrType = "M")
  private val ducrBlock = UcrBlock(ucr = validDucr, ucrType = "D")
  private val ducrPartBlock = UcrBlock(ucr = validWholeDucrParts, ucrType = "DP")

  "Submission Frontend Model" should {

    "return  the expected result from the hasMucr method" in {
      submission(mucrBlock).hasMucr mustBe true
      submission(ducrBlock, mucrBlock).hasMucr mustBe true
      submission(ducrBlock).hasMucr mustBe false
    }

    "return  the expected result from the hasDucr method" in {
      submission(ducrBlock).hasDucr mustBe true
      submission(mucrBlock, ducrBlock).hasDucr mustBe true
      submission(mucrBlock).hasDucr mustBe false
    }

    "return  the expected result from the hasDucrPart method" in {
      submission(ducrPartBlock).hasDucrPart mustBe true
      submission(ducrPartBlock, ducrBlock).hasDucrPart mustBe true
      submission(ducrBlock).hasDucrPart mustBe false
    }

    "return  the expected result from the extractUcr method" in {
      submission().extractUcr mustBe None

      submission(mucrBlock).extractUcr.get mustBe "MUCR" -> validMucr
      submission(ducrBlock, mucrBlock).extractUcr.get mustBe "MUCR" -> validMucr

      submission(ducrBlock).extractUcr.get mustBe "DUCR" -> validDucr
      submission(ducrBlock, ducrPartBlock).extractUcr.get mustBe "DUCR" -> validDucr

      submission(ducrPartBlock).extractUcr.get mustBe "DUCR Part" -> validWholeDucrParts
      submission(ducrPartBlock, ducrBlock).extractUcr.get mustBe "DUCR Part" -> validWholeDucrParts
    }
  }
}
