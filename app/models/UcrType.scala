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

package models

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Format, JsString, JsonValidationError, Reads, Writes}

sealed abstract class UcrType(val formValue: String, val codeValue: String)

object UcrType {
  case object Mucr extends UcrType("mucr", "M")
  case object Ducr extends UcrType("ducr", "D")

  private val lookup = PartialFunction[String, UcrType] {
    case Mucr.formValue => Mucr
    case Ducr.formValue => Ducr
  }

  val formatter: Formatter[UcrType] = new Formatter[UcrType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], UcrType] = {
      data.get(key).map { typ =>
        lookup.andThen(Right.apply).applyOrElse(typ, (_: String) => Left(Seq(FormError(key, "error.unknown"))))
      }
    }.getOrElse(Left(Seq(FormError(key, "error.required"))))

    override def unbind(key: String, value: UcrType): Map[String, String] = Map(key -> value.formValue)
  }

  implicit val format =
    Format[UcrType](Reads.StringReads.collect(JsonValidationError("error.unknown"))(lookup), Writes(kind => JsString(kind.formValue)))
}
