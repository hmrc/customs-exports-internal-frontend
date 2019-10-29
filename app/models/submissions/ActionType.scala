package models.submissions

import play.api.libs.json._

sealed abstract class ActionType(val value: String)

object ActionType {
  case object Arrival extends ActionType("Arrival")
  case object Departure extends ActionType("Departure")
  case object DucrAssociation extends ActionType("DucrAssociation")
  case object MucrAssociation extends ActionType("MucrAssociation")
  case object DucrDisassociation extends ActionType("DucrDisassociation")
  case object MucrDisassociation extends ActionType("MucrDisassociation")
  case object ShutMucr extends ActionType("ShutMucr")

  implicit val format = new Format[ActionType] {
    override def writes(actionType: ActionType): JsValue = JsString(actionType.value)

    override def reads(json: JsValue): JsResult[ActionType] = json match {
      case JsString("Arrival")            => JsSuccess(Arrival)
      case JsString("Departure")          => JsSuccess(Departure)
      case JsString("DucrAssociation")    => JsSuccess(DucrAssociation)
      case JsString("MucrAssociation")    => JsSuccess(MucrAssociation)
      case JsString("DucrDisassociation") => JsSuccess(DucrDisassociation)
      case JsString("MucrDisassociation") => JsSuccess(MucrDisassociation)
      case JsString("ShutMucr")           => JsSuccess(ShutMucr)
      case _                              => JsError("Unknown ActionType")
    }
  }
}
