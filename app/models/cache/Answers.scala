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

package models.cache

import forms.{AssociateUcr, MucrOptions, _}
import models.UcrBlock
import models.UcrBlock.{ducr, mucr}
import models.cache.JourneyType.JourneyType
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.json.Union

sealed trait Answers {
  val `type`: JourneyType
  val eori: Option[String]
}

object Answers {
  implicit val format: Format[Answers] = Union
    .from[Answers]("type")
    .and[ArrivalAnswers](JourneyType.ARRIVE.toString)
    .and[RetrospectiveArrivalAnswers](JourneyType.RETROSPECTIVE_ARRIVE.toString)
    .and[DepartureAnswers](JourneyType.DEPART.toString)
    .and[AssociateUcrAnswers](JourneyType.ASSOCIATE_UCR.toString)
    .and[DisassociateUcrAnswers](JourneyType.DISSOCIATE_UCR.toString)
    .and[ShutMucrAnswers](JourneyType.SHUT_MUCR.toString)
    .format

  val fakeEORI = Some("GB1234567890")
}

sealed trait MovementAnswers extends Answers {
  val consignmentReferences: Option[ConsignmentReferences]
  val location: Option[Location]
}

case class ArrivalAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  arrivalReference: Option[ArrivalReference] = None,
  arrivalDetails: Option[ArrivalDetails] = None,
  override val location: Option[Location] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.ARRIVE
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]

  def apply(queryUcr: Option[UcrBlock]): ArrivalAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducr) =>
      ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, ucrBlock.ucr)))
    case _ =>
      ArrivalAnswers()
  }
}

case class RetrospectiveArrivalAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  override val location: Option[Location] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.RETROSPECTIVE_ARRIVE
}

object RetrospectiveArrivalAnswers {
  implicit val format: Format[RetrospectiveArrivalAnswers] = Json.format[RetrospectiveArrivalAnswers]

  def apply(queryUcr: Option[UcrBlock]): RetrospectiveArrivalAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducr) =>
      RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, ucrBlock.ucr)))
    case _ =>
      RetrospectiveArrivalAnswers()
  }
}

case class DepartureAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  departureDetails: Option[DepartureDetails] = None,
  override val location: Option[Location] = None,
  goodsDeparted: Option[GoodsDeparted] = None,
  transport: Option[Transport] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.DEPART
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]

  def apply(queryUcr: Option[UcrBlock]): DepartureAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducr) =>
      DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, ucrBlock.ucr)))
    case _ =>
      DepartureAnswers()
  }
}

case class AssociateUcrAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  mucrOptions: Option[MucrOptions] = None,
  associateUcr: Option[AssociateUcr] = None
) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ASSOCIATE_UCR
}

object AssociateUcrAnswers {
  implicit val format: Format[AssociateUcrAnswers] = Json.format[AssociateUcrAnswers]

  def apply(queryUcr: Option[UcrBlock]): AssociateUcrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      AssociateUcrAnswers(associateUcr = Some(AssociateUcr(AssociateKind.Mucr, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducr) =>
      AssociateUcrAnswers(associateUcr = Some(AssociateUcr(AssociateKind.Ducr, ucrBlock.ucr)))
    case _ =>
      AssociateUcrAnswers()
  }
}

case class DisassociateUcrAnswers(override val eori: Option[String] = Answers.fakeEORI, ucr: Option[DisassociateUcr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object DisassociateUcrAnswers {
  implicit val format: Format[DisassociateUcrAnswers] = Json.format[DisassociateUcrAnswers]

  def apply(queryUcr: Option[UcrBlock]): DisassociateUcrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      DisassociateUcrAnswers(ucr = Some(DisassociateUcr(DisassociateKind.Mucr, None, Some(ucrBlock.ucr))))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducr) =>
      DisassociateUcrAnswers(ucr = Some(DisassociateUcr(DisassociateKind.Ducr, Some(ucrBlock.ucr), None)))
    case _ =>
      DisassociateUcrAnswers()
  }
}

case class ShutMucrAnswers(override val eori: Option[String] = Answers.fakeEORI, shutMucr: Option[ShutMucr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.SHUT_MUCR
}

object ShutMucrAnswers {
  implicit val format: Format[ShutMucrAnswers] = Json.format[ShutMucrAnswers]

  def apply(queryUcr: Option[UcrBlock]): ShutMucrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucr) =>
      ShutMucrAnswers(shutMucr = Some(ShutMucr(ucrBlock.ucr)))
    case _ =>
      ShutMucrAnswers()
  }
}
