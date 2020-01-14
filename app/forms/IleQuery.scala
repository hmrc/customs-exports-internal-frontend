package forms

import play.api.data.{Form, Forms}
import play.api.data.Forms.text
import utils.FieldValidator._

object IleQuery {

  val form: Form[String] = Form(
    Forms.single(
      "ucr" -> text()
        .verifying("ileQuery.ucr.empty", nonEmpty)
        .verifying("ileQuery.ucr.incorrect", isEmpty or validDucr or validMucr)
    )
  )
}
