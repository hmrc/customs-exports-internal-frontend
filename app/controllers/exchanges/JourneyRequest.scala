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

package controllers.exchanges

import models.ReturnToStartException
import models.cache.{Answers, Cache}
import play.api.mvc.WrappedRequest

import scala.reflect.ClassTag

case class JourneyRequest[T](cache: Cache, request: AuthenticatedRequest[T]) extends WrappedRequest(request) {

  val operator: Operator = request.operator
  val providerId: String = request.operator.providerId

  def answers: Answers = cache.answers.getOrElse(throw ReturnToStartException)

  def answersAs[J <: Answers](implicit tag: ClassTag[J]): J =
    answers match {
      case ans: J => ans
      case _      => throw ReturnToStartException
    }
}
