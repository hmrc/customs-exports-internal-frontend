package models

import play.api.libs.json.Json

case class UcrBlock(ucr: String, ucrType: String)

object UcrBlock {
  implicit val format = Json.format[UcrBlock]
}
