/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.storage

import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import play.api.mvc.Request
import forms.ConsignmentReferences

class FlashExtractor {

  def extractValue(key: String, request: Request[_]): Option[String] = request.flash.get(key)

  def extractMovementType(request: Request[_]): Option[JourneyType] =
    extractValue(FlashExtractor.MOVEMENT_TYPE, request).map(JourneyType.withName)

  def extractUcr(request: Request[_]): Option[String] =
    extractValue(FlashExtractor.UCR, request)

  def extractUcrType(request: Request[_]): Option[String]=
    extractValue(FlashExtractor.UCR_TYPE, request)

  def extractMucrToAssociate(request: Request[_]): Option[String] =
    extractValue(FlashExtractor.MUCR_TO_ASSOCIATE, request)

  def extractConsignmentRefs(request: Request[_]): Option[ConsignmentReferences] =
    for {
      ucr <- extractUcr(request)
      ucrType <- extractUcrType(request)
    } yield ConsignmentReferences(ucrType, ucr)

}

object FlashExtractor {
  val MOVEMENT_TYPE: String = "MOVEMENT_TYPE"
  val UCR = "UCR"
  val UCR_TYPE = "UCR_TYPE"
  val MUCR_TO_ASSOCIATE = "MUCR_TO_ASSOCIATE"
}
