package models.submissions

import java.time.Instant
import java.util.UUID

import models.UcrBlock
import play.api.libs.json._

case class SubmissionFrontendModel(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = Instant.now()
) {

  def hasMucr: Boolean = ucrBlocks.exists(_.ucrType == "M")

  def extractMucr: Option[String] = ucrBlocks.find(_.ucrType == "M").map(_.ucr)

  def extractFirstUcr: Option[String] = ucrBlocks.headOption.map(_.ucr)
}

object SubmissionFrontendModel {
  implicit val formats = Json.format[SubmissionFrontendModel]
}
