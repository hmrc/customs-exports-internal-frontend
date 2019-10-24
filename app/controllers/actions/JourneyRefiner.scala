/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.actions

import controllers.exchanges.{AuthenticatedRequest, JourneyRequest}
import models.cache.JourneyType.JourneyType
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.MovementRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

abstract class JourneyRefiner[J, +R[_] <: JourneyRequest[_]](movementRepository: MovementRepository) extends ActionRefiner[AuthenticatedRequest, R]{

  val `type`: JourneyType
  def requestGenerator[A](request: AuthenticatedRequest[A], answers: J): R[A]

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, R[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    val eventualJ: Future[Option[J]] = movementRepository.findByPid(request.operator.pid)
      .map(_.filter(_.answers.`type` == `type`))
      .map(_.map(_.answers.asInstanceOf[J]))

    eventualJ
      .map {
        case Some(answers: J) => Right(requestGenerator(request, answers))
        case _ => Left(Results.Redirect(controllers.routes.ChoiceController.displayChoiceForm()))
      }
  }

}
