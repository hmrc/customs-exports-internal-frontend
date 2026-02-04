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

package models.summary

import forms.ConsignmentReferences
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import models.summary.SessionHelper._
import play.api.mvc.Request

case class Confirmation(journeyType: JourneyType, conversationId: String, consignmentRefs: Option[ConsignmentReferences], mucr: Option[String])

object Confirmation {

  def apply()(implicit request: Request[?]): Option[Confirmation] = {
    val consignmentReferences =
      for {
        ucr <- getValue(UCR)
        ucrType <- getValue(UCR_TYPE)
      } yield ConsignmentReferences(ucrType, ucr)

    for {
      journeyType <- getValue(JOURNEY_TYPE).map(JourneyType.withName)
      conversationId <- getValue(CONVERSATION_ID)
    } yield new Confirmation(journeyType, conversationId, consignmentReferences, getValue(MUCR_TO_ASSOCIATE))
  }
}
