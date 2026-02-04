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

import models.{ReturnToStartException, UcrType}
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import utils.FieldValidator._

case class DisassociateUcr(kind: UcrType, ducr: Option[String], mucr: Option[String]) {
  def ucr: String = kind match {
    case UcrType.Mucr                    => mucr.getOrElse(throw ReturnToStartException)
    case UcrType.Ducr | UcrType.DucrPart => ducr.getOrElse(throw ReturnToStartException)
  }
}

object DisassociateUcr {
  import uk.gov.voa.play.form.ConditionalMappings._

  val formId: String = "DisassociateUcr"

  implicit val format: OFormat[DisassociateUcr] = Json.format[DisassociateUcr]

  val mapping: Mapping[DisassociateUcr] =
    Forms.mapping(
      "kind" -> of[UcrType](UcrType.formatter),
      "ducr" -> mandatoryIfEqual(
        "kind",
        "ducr",
        text()
          .verifying("disassociate.ucr.ducr.empty", nonEmpty)
          .verifying("disassociate.ucr.ducr.error", isEmpty or validDucrIgnoreCase)
      ),
      "mucr" -> mandatoryIfEqual(
        "kind",
        "mucr",
        text()
          .verifying("disassociate.ucr.mucr.empty", nonEmpty)
          .verifying("disassociate.ucr.mucr.error", isEmpty or validMucrIgnoreCase)
      )
    )(form2Data)(disassociateUcrForm => Option((disassociateUcrForm.kind, disassociateUcrForm.ducr, disassociateUcrForm.mucr)))

  private def form2Data(kind: UcrType, ducr: Option[String], mucr: Option[String]): DisassociateUcr =
    new DisassociateUcr(kind, ducr.map(_.trim.toUpperCase), mucr.map(_.trim.toUpperCase))

  val form: Form[DisassociateUcr] = Form(mapping)
}
