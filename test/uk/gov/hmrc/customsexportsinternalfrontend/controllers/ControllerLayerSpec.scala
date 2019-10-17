package uk.gov.hmrc.customsexportsinternalfrontend.controllers

import org.mockito.BDDMockito
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{MessagesControllerComponents, Request, Result, Results}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.customsexportsinternalfrontend.config.AppConfig
import uk.gov.hmrc.customsexportsinternalfrontend.connectors.StrideAuthConnector
import uk.gov.hmrc.customsexportsinternalfrontend.controllers.actions.AuthenticatedAction
import uk.gov.hmrc.customsexportsinternalfrontend.controllers.exchanges.{AuthenticatedRequest, Operator}
import uk.gov.hmrc.customsexportsinternalfrontend.views.html.unauthorized
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class ControllerLayerSpec extends WordSpec with MustMatchers with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  protected def aRealInstanceOf[T: ClassTag]: T = app.injector.instanceOf[T]

  case class SuccessfulAuth(operator: Operator = Operator("0")) extends AuthenticatedAction(
    mock[AppConfig],
    mock[Configuration],
    mock[Environment],
    mock[StrideAuthConnector],
    stubMessagesControllerComponents(),
    mock[unauthorized]
  ) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = block(AuthenticatedRequest(operator, request))
  }

  case object UnsuccessfulAuth extends AuthenticatedAction(
    mock[AppConfig],
    mock[Configuration],
    mock[Environment],
    mock[StrideAuthConnector],
    stubMessagesControllerComponents(),
    mock[unauthorized]
  ) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = Future.successful(Results.Forbidden(""))
  }
}
