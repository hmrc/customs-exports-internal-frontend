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

package forms

import forms.Choice._
import models.cache.Cache
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}

object Movement {

  def createMovementRequest(cache: Cache): MovementRequest =
    // TODO - implement
    MovementRequest(
      eori = "TODO",
      choice = MovementType.Arrival,
      consignmentReference = ConsignmentReferences("todo", "todo"),
      movementDetails = MovementDetailsRequest("todo"),
      location = None,
      transport = None,
      arrivalReference = None
    )

  private def extractChoice(choice: Choice) = choice match {
    case Arrival   => MovementType.Arrival
    case Departure => MovementType.Departure
    case _         => throw new IllegalArgumentException("Allowed is only arrival or departure here")
  }
}
