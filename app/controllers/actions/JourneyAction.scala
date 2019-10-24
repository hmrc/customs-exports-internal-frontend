package controllers.actions

import controllers.exchanges.{AuthenticatedRequest, JourneyRequest}
import javax.inject.Inject
import models.cache.Answers
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.MovementRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject()(movementRepository: MovementRepository)(implicit override val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    movementRepository.findByPid(request.operator.pid).map {
      case Some(cache) => Right(JourneyRequest(cache.answers, request))
      case None => Left(Results.Redirect(controllers.routes.ChoiceController.displayChoiceForm()))
    }
  }
}
