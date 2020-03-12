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

package controllers.movements

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import forms.GoodsDeparted
import forms.GoodsDeparted.form
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{Cache, DepartureAnswers, JourneyType, MovementAnswers}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.goods_departed

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsDepartedController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  goodsDepartedPage: goods_departed
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)) { implicit request =>
    val answers = request.answersAs[DepartureAnswers]
    val goodsDeparted = answers.goodsDeparted
    val consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)

    Ok(goodsDepartedPage(goodsDeparted.fold(form)(form.fill(_)), consignmentReference))
  }

  def saveGoodsDeparted(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)).async { implicit request =>
    val consignmentReference =
      request.answersAs[MovementAnswers].consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[GoodsDeparted]) => Future.successful(BadRequest(goodsDepartedPage(formWithErrors, consignmentReference))),
        validGoodsDeparted => {
          val updatedAnswers = request.answersAs[DepartureAnswers].copy(goodsDeparted = Some(validGoodsDeparted))

          cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
            Redirect(controllers.movements.routes.TransportController.displayPage())
          }
        }
      )
  }

}
