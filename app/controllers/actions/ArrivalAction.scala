package controllers.actions

import controllers.exchanges.{ArrivalRequest, AuthenticatedRequest}
import javax.inject.Inject
import models.cache.{Arrival, JourneyType}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import repositories.MovementRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class ArrivalAction @Inject()(movementRepository: MovementRepository)
                             (implicit override val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedRequest, ArrivalRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, ArrivalRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    movementRepository.findByPid(request.operator.pid)
      .map(_.filter(_.answers.`type` == JourneyType.ARRIVE))
      .map(_.map(_.answers.asInstanceOf[Arrival]))
      .map {
        case Some(arrival: Arrival) => Right(ArrivalRequest(arrival, request))
        case _ => Left(Redirect(controllers.routes.ChoiceController.displayChoiceForm()))
      }
  }

}