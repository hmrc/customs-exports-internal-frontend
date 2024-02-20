/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.data.{FieldMapping, Form, FormError, Forms, Mapping}
import play.api.data.Forms.{of, text}
import play.api.data.format.Formatter
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.FieldValidator._

object IleQueryForm {

  val cds = "cds"
  val chief = "chief"

  private val allowedValues = Seq(cds, chief)

  val isIleQuery = "isIleQuery"
  val ucr = "ucr"

  val formId = "manageChiefOrCds"

  private val cdsUcrMapping: Mapping[String] =
    text()
      .verifying("ileQuery.ucr.empty", nonEmpty)
      .verifying("ileQuery.ucr.incorrect", isEmpty or validDucrIgnoreCase or validMucrIgnoreCase)
      .transform[String](_.trim.toUpperCase, identity)

  val mapping: Mapping[Option[String]] =
    Forms.mapping(isIleQuery -> requiredRadio("ileQuery.radio.empty", allowedValues), ucr -> mandatoryIfEqual(isIleQuery, cds, cdsUcrMapping))(
      (_, ucr) => ucr
    )(unapply)

  private def unapply(ucr: Option[String]): Option[(String, Option[String])] =
    if (ucr.isDefined) Some((cds, ucr)) else Some((chief, None))

  def form: Form[Option[String]] = Form(IleQueryForm.mapping)

  def requiredRadio(requiredKey: String, choices: Seq[String]): FieldMapping[String] =
    of(radioFormatter(requiredKey, choices))

  private def radioFormatter(requiredKey: String, choices: Seq[String], args: Seq[Any] = List.empty): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case Some("")                       => Left(List(FormError(key, requiredKey, args)))
        case Some(s) if choices.isEmpty     => Right(s)
        case Some(s) if choices.contains(s) => Right(s)
        case _                              => Left(List(FormError(key, requiredKey, args)))
      }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }
}
