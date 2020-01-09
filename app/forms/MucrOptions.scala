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

package forms

import forms.EnhancedMapping.{mucrOptionsFormatter, requiredRadio}
import forms.MucrOptions.CreateOrAddValues
import forms.MucrOptions.CreateOrAddValues.{Add, Create}
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json._

case class MucrOptions(newMucr: String = "", existingMucr: String = "", createOrAdd: CreateOrAddValues = Create) {
  def mucr: String = createOrAdd match {
    case Create => newMucr
    case Add    => existingMucr
  }
}

object MucrOptions {

  val formId = "MucrOptions"

  def form2Model: (String, String, String) => MucrOptions = {
    case (createOrAdd, newMucr, existingMucr) =>
      createOrAdd match {
        case Create.value => MucrOptions(newMucr, "", Create)
        case Add.value    => MucrOptions("", existingMucr, Add)
      }
  }

  def model2Form: MucrOptions => Option[(String, String, String)] =
    m => Some((m.createOrAdd.value, m.newMucr, m.existingMucr))

  val mapping = Forms.mapping(
    "createOrAdd" -> requiredRadio("mucrOptions.createAdd.value.empty"),
    "newMucr" -> of(mucrOptionsFormatter(Create)),
    "existingMucr" -> of(mucrOptionsFormatter(Add))
  )(form2Model)(model2Form)

  def form: Form[MucrOptions] = Form(mapping)

  sealed abstract class CreateOrAddValues(val value: String)
  object CreateOrAddValues {
    case object Create extends CreateOrAddValues(value = "create")
    case object Add extends CreateOrAddValues(value = "add")

    def apply(input: String): CreateOrAddValues = input match {
      case Create.value => Create
      case Add.value    => Add
    }

    implicit object CreateOrAddValuesFormat extends Format[CreateOrAddValues] {
      override def reads(json: JsValue): JsResult[CreateOrAddValues] = json match {
        case JsString(createOrAddValue) => JsSuccess(CreateOrAddValues(createOrAddValue))
        case _                          => JsError("Incorrect CreateOrAddValues")
      }

      override def writes(o: CreateOrAddValues): JsValue = JsString(o.value)
    }
  }

  implicit val format = Json.format[MucrOptions]
}
