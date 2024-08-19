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

import play.api.mvc.{Request, Session}

object SessionHelper {

  val JOURNEY_TYPE: String = "JOURNEY_TYPE"
  val UCR = "UCR"
  val UCR_TYPE = "UCR_TYPE"
  val MUCR_TO_ASSOCIATE = "MUCR_TO_ASSOCIATE"
  val CONVERSATION_ID = "CONVERSATION_ID"

  def getValue(key: String)(implicit request: Request[_]): Option[String] =
    request.session.data.get(key)

  def getOrElse(key: String, default: String = "")(implicit request: Request[_]): String =
    request.session.data.getOrElse(key, default)

  def removeValue(key: String)(implicit request: Request[_]): Session =
    request.session - key
}
