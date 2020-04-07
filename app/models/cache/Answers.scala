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
import models.{UcrBlock, UcrType}
import models.UcrBlock.{ducrType, mucrType}
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
  val specificDateTimeChoice: Option[SpecificDateTimeChoice]
}

case class ArrivalAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  override val specificDateTimeChoice: Option[SpecificDateTimeChoice] = None,
  arrivalDetails: Option[ArrivalDetails] = None,
  override val location: Option[Location] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.ARRIVE
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]

  def fromQueryUcr(queryUcr: Option[UcrBlock]): ArrivalAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducrType) =>
      ArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, ucrBlock.ucr)))
    case _ =>
      ArrivalAnswers()
  }
}

case class RetrospectiveArrivalAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  override val specificDateTimeChoice: Option[SpecificDateTimeChoice] = None,
  override val location: Option[Location] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.RETROSPECTIVE_ARRIVE
}

object RetrospectiveArrivalAnswers {
  implicit val format: Format[RetrospectiveArrivalAnswers] = Json.format[RetrospectiveArrivalAnswers]

  def fromQueryUcr(queryUcr: Option[UcrBlock]): RetrospectiveArrivalAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      RetrospectiveArrivalAnswers(
        consignmentReferences = Some(ConsignmentReferences(reference = ConsignmentReferenceType.M, referenceValue = ucrBlock.ucr))
      )
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducrType) =>
      RetrospectiveArrivalAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.D, ucrBlock.ucr)))
    case _ =>
      RetrospectiveArrivalAnswers()
  }
}

case class DepartureAnswers(
  override val eori: Option[String] = Answers.fakeEORI,
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  override val specificDateTimeChoice: Option[SpecificDateTimeChoice] = None,
  departureDetails: Option[DepartureDetails] = None,
  override val location: Option[Location] = None,
  goodsDeparted: Option[GoodsDeparted] = None,
  transport: Option[Transport] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.DEPART
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]

  def fromQueryUcr(queryUcr: Option[UcrBlock]): DepartureAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      DepartureAnswers(consignmentReferences = Some(ConsignmentReferences(ConsignmentReferenceType.M, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducrType) =>
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

  def fromQueryUcr(queryUcr: Option[UcrBlock]): AssociateUcrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      AssociateUcrAnswers(associateUcr = Some(AssociateUcr(UcrType.Mucr, ucrBlock.ucr)))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducrType) =>
      AssociateUcrAnswers(associateUcr = Some(AssociateUcr(UcrType.Ducr, ucrBlock.ucr)))
    case _ =>
      AssociateUcrAnswers()
  }
}

case class DisassociateUcrAnswers(override val eori: Option[String] = Answers.fakeEORI, ucr: Option[DisassociateUcr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object DisassociateUcrAnswers {
  implicit val format: Format[DisassociateUcrAnswers] = Json.format[DisassociateUcrAnswers]

  def fromQueryUcr(queryUcr: Option[UcrBlock]): DisassociateUcrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      DisassociateUcrAnswers(ucr = Some(DisassociateUcr(UcrType.Mucr, None, Some(ucrBlock.ucr))))
    case Some(ucrBlock) if ucrBlock.ucrType.equals(ducrType) =>
      DisassociateUcrAnswers(ucr = Some(DisassociateUcr(UcrType.Ducr, Some(ucrBlock.ucr), None)))
    case _ =>
      DisassociateUcrAnswers()
  }
}

case class ShutMucrAnswers(override val eori: Option[String] = Answers.fakeEORI, shutMucr: Option[ShutMucr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.SHUT_MUCR
}

object ShutMucrAnswers {
  implicit val format: Format[ShutMucrAnswers] = Json.format[ShutMucrAnswers]

  def fromQueryUcr(queryUcr: Option[UcrBlock]): ShutMucrAnswers = queryUcr match {
    case Some(ucrBlock) if ucrBlock.ucrType.equals(mucrType) =>
      ShutMucrAnswers(shutMucr = Some(ShutMucr(ucrBlock.ucr)))
    case _ =>
      ShutMucrAnswers()
  }
}
