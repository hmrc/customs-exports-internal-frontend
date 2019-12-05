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
import controllers.storage.FlashKeys
import javax.inject.Inject
import models.cache._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.summary.{arrival_summary_page, departure_summary_page, retrospective_arrival_summary_page}

import scala.concurrent.ExecutionContext

class MovementSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  arrivalSummaryPage: arrival_summary_page,
  retrospectiveArrivalSummaryPage: retrospective_arrival_summary_page,
  departureSummaryPage: departure_summary_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.RETROSPECTIVE_ARRIVE, JourneyType.DEPART)) { implicit request =>
      request.answers match {
        case arrivalAnswers: ArrivalAnswers                   => Ok(arrivalSummaryPage(arrivalAnswers))
        case retroArrivalAnswers: RetrospectiveArrivalAnswers => Ok(retrospectiveArrivalSummaryPage(retroArrivalAnswers))
        case departureAnswers: DepartureAnswers               => Ok(departureSummaryPage(departureAnswers))
      }
    }

  def submitMovementRequest(): Action[AnyContent] =
    (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.RETROSPECTIVE_ARRIVE, JourneyType.DEPART)).async { implicit request =>
      submissionService
        .submit(request.providerId, request.answersAs[MovementAnswers])
        .map { consignmentReferences =>
          Redirect(controllers.movements.routes.MovementConfirmationController.display())
            .flashing(
              FlashKeys.MOVEMENT_TYPE -> request.answers.`type`.toString,
              FlashKeys.UCR_KIND -> consignmentReferences.reference.toString,
              FlashKeys.UCR -> consignmentReferences.referenceValue
            )
        }
    }
}
