/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.MucrOptions.CreateOrAddValues
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError, Forms}
import utils.FieldValidator.validMucrIgnoreCase

object EnhancedMapping {

  def requiredRadio(requiredKey: String = "error.required"): FieldMapping[String] =
    Forms.of(radioFormatter(requiredKey))

  private def radioFormatter(requiredKey: String): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None | Some("") => Left(Seq(FormError(key, requiredKey)))
        case Some(s)         => Right(s)
      }
    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  def mucrOptionsFormatter(createOrAddValue: CreateOrAddValues): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = data.get("createOrAdd") match {
      case Some(createOrAddValue.`value`) =>
        val mucr = data.getOrElse(key, "")

        if (mucr.isEmpty) Left(Seq(FormError(key, "mucrOptions.reference.value.empty")))
        else {
          if (validMucrIgnoreCase(mucr)) Right(mucr)
          else Left(Seq(FormError(key, "mucrOptions.reference.value.error")))
        }

      case _ => Right("")
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

}
