/*
 * Copyright 2025 HM Revenue & Customs
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

/**
 * Copied from the play-json-union-formatter library.
 * That library is not being upgraded to Scala 3, and been advised to copy this single Scala file over.
 * @see <a href="https://github.com/hmrc/play-json-union-formatter/blob/main/README.md">play-json-union-formatter</a>
 */

package utils

import scala.reflect.ClassTag

import play.api.libs.json._

class Union[A](typeField: String, readWith: PartialFunction[String, JsValue => JsResult[A]], writeWith: PartialFunction[A, JsObject]) {

  def andLazy[B <: A](typeTag: String, fmt: => OFormat[B])(implicit ct: ClassTag[B]) = {
    val readCase: PartialFunction[String, JsValue => JsResult[A]] = { case `typeTag` =>
      (jsValue: JsValue) => Json.fromJson(jsValue)(fmt).asInstanceOf[JsResult[A]]
    }

    val writeCase: PartialFunction[A, JsObject] = { case value: B =>
      Json.toJsObject(value)(fmt) ++ Json.obj(typeField -> typeTag)
    }

    new Union(typeField, readWith.orElse(readCase), writeWith.orElse(writeCase))
  }

  def and[B <: A](typeTag: String)(implicit ct: ClassTag[B], f: OFormat[B]) =
    andLazy(typeTag, f)

  def andLazyType[B <: A](typeTag: String, fn: () => B)(implicit ct: ClassTag[B]) = {
    val readCase: PartialFunction[String, JsValue => JsResult[A]] = { case `typeTag` =>
      _ => JsSuccess(fn())
    }

    val writeCase: PartialFunction[A, JsObject] = { case _: B =>
      Json.obj(typeField -> typeTag)
    }

    new Union(typeField, readWith.orElse(readCase), writeWith.orElse(writeCase))
  }

  def andType[B <: A](typeTag: String, fn: () => B)(implicit tt: ClassTag[B]) =
    andLazyType(typeTag, fn)

  private val defaultReads: PartialFunction[String, JsValue => JsResult[A]] = { case attemptedType =>
    _ => JsError(__ \ typeField, s"$attemptedType is not a recognised $typeField")
  }

  def format: OFormat[A] = {
    val reads = Reads[A] { json =>
      (json \ typeField).validate[String].flatMap { typeTag =>
        readWith.orElse(defaultReads)(typeTag)(json)
      }
    }
    val writes = OWrites[A](writeWith)
    OFormat(reads, writes)
  }
}

object Union {
  def from[A](typeField: String) = new Union[A](typeField, PartialFunction.empty, PartialFunction.empty)
}
