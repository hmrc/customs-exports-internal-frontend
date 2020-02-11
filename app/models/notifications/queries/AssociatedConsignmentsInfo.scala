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

package models.notifications.queries

import models.viewmodels.decoder.ROECode

case class AssociatedConsignmentsInfo(childDucrs: Seq[DucrInfo] = Seq.empty, childMucrs: Seq[MucrInfo] = Seq.empty) {

  def size: Int = childDucrs.size + childMucrs.size

  def mostSevereRoe: Option[String] =
    (childDucrs ++ childMucrs)
      .sortBy(_.entryStatus.flatMap(_.roe).getOrElse(ROECode.UnknownRoe))
      .headOption
      .flatMap(_.entryStatus.flatMap(_.roe.map(_.code)))
}
