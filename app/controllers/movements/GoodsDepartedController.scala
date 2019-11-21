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

package controllers.movements

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import forms.GoodsDeparted
import forms.GoodsDeparted.form
import javax.inject.{Inject, Singleton}
import models.cache.{Cache, DepartureAnswers, JourneyType}
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
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  goodsDepartedPage: goods_departed
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)) { implicit request =>
    val goodsDeparted = request.answersAs[DepartureAnswers].goodsDeparted

    Ok(goodsDepartedPage(goodsDeparted.fold(form)(form.fill(_))))
  }

  def saveGoodsDeparted(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[GoodsDeparted]) => Future.successful(BadRequest(goodsDepartedPage(formWithErrors))),
        validGoodsDeparted => {
          val updatedCache = request.answersAs[DepartureAnswers].copy(goodsDeparted = Some(validGoodsDeparted))

          cache.upsert(Cache(request.providerId, updatedCache)).map { _ =>
            Redirect(controllers.movements.routes.TransportController.displayPage())
          }
        }
      )
  }

}