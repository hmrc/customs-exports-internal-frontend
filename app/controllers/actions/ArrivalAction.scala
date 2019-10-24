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

package controllers.actions

import controllers.exchanges.{ArrivalRequest, AuthenticatedRequest}
import models.cache.Arrival
import repositories.MovementRepository

import scala.concurrent.ExecutionContext

class ArrivalAction(movementRepository: MovementRepository)
                   (override val executionContext: ExecutionContext) extends JourneyRefiner[Arrival, ArrivalRequest](movementRepository) {
  override def requestGenerator[A](request: AuthenticatedRequest[A], answers: Arrival): ArrivalRequest[A] = ArrivalRequest(answers, request)
}