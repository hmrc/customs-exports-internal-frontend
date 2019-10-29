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

package testdata
import forms.{Choice, ConsignmentReferences, Movement}
import models.cache.{ArrivalAnswers, DepartureAnswers}
import models.requests.MovementRequest

object MovementsTestData {

  def validMovementRequest(movementType: Choice): MovementRequest =
    movementType match {
      case Choice.Arrival   => Movement.createMovementRequest("pid", validArrivalAnswers)
      case Choice.Departure => Movement.createMovementRequest("pid", validDepartureAnswers)
    }

  def validArrivalAnswers =
    ArrivalAnswers(Some("eori"), consignmentReferences = Some(ConsignmentReferences("ref", "value")))

  def validDepartureAnswers =
    DepartureAnswers(Some("eori"), consignmentReferences = Some(ConsignmentReferences("ref", "value")))
}
