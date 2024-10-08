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

package forms

import forms.EnhancedMapping.requiredRadio
import models.cache.JourneyType.{JourneyType, _}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import play.api.mvc.PathBindable
import utils.FieldValidator.isContainedIn

sealed abstract class Choice(val value: String) {

  def isArrival: Boolean = value == Choice.Arrival.value
  def isDeparture: Boolean = value == Choice.Departure.value
}

object Choice {

  def unapply(status: Choice): Option[String] = Some(status.value)

  def apply(input: String): Choice =
    allChoices.find(_.value == input).getOrElse(throw new IllegalArgumentException("Incorrect choice"))

  def apply(`type`: JourneyType): Choice =
    (`type`: @unchecked) match {
      case ARRIVE               => Arrival
      case RETROSPECTIVE_ARRIVE => RetrospectiveArrival
      case DEPART               => Departure
      case ASSOCIATE_UCR        => AssociateUCR
      case DISSOCIATE_UCR       => DisassociateUCR
      case SHUT_MUCR            => ShutMUCR
    }

  implicit object ChoiceValueFormat extends Format[Choice] {
    def reads(status: JsValue): JsResult[Choice] = status match {
      case JsString(choice) =>
        allChoices.find(_.value == choice).map(JsSuccess(_)).getOrElse(JsError("Incorrect choice"))
      case _ => JsError("Incorrect choice")
    }

    def writes(choice: Choice): JsValue = JsString(choice.value)
  }

  implicit val bindable: PathBindable[Choice] = new PathBindable[Choice] {
    override def bind(key: String, choice: String): Either[String, Choice] = allChoices.find(_.value == choice).toRight[String]("Invalid Choice")

    override def unbind(key: String, choice: Choice): String = choice.value
  }

  case object Arrival extends Choice("arrival")
  case object RetrospectiveArrival extends Choice("retrospectiveArrival")
  case object Departure extends Choice("departure")
  case object AssociateUCR extends Choice("associateUCR")
  case object DisassociateUCR extends Choice("disassociateUCR")
  case object ShutMUCR extends Choice("shutMUCR")

  val allChoices: Seq[Choice] = Seq(Arrival, RetrospectiveArrival, Departure, AssociateUCR, DisassociateUCR, ShutMUCR)

  val choiceId = "choice"

  val choiceMapping: Mapping[Choice] =
    Forms.mapping(
      choiceId -> requiredRadio("choicePage.input.error.empty")
        .verifying("choicePage.input.error.incorrectValue", isContainedIn(allChoices.map(_.value)))
    )(Choice.apply)(Choice.unapply)

  def form(): Form[Choice] = Form(choiceMapping)
}
