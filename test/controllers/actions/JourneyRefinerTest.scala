package controllers.actions

import controllers.exchanges.{AuthenticatedRequest, JourneyRequest, Operator}
import models.cache.{ArrivalAnswers, Cache}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import repositories.MovementRepository
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyRefinerTest extends WordSpec with MustMatchers with MockitoSugar with BeforeAndAfterEach {

  private val movementRepository = mock[MovementRepository]
  private val block = mock[JourneyRequest[_] =>  Future[Result]]
  private val operator = Operator("pid")
  private val request = AuthenticatedRequest(operator, FakeRequest())
  private val answers = ArrivalAnswers()
  private val cache = Cache("pid", answers)

  private val refiner = new JourneyRefiner(movementRepository)

  override def afterEach(): Unit = {
    reset(movementRepository, block)
    super.afterEach()
  }

  "refine" should {
    "permit request" when {
      "answers found" in {
        given(block.apply(any())).willReturn(Future.successful(Results.Ok))
        given(movementRepository.findByPid("pid")).willReturn(Future.successful(Some(cache)))

        await(refiner.invokeBlock(request, block)) mustBe Results.Ok

        theRequestBuilt mustBe JourneyRequest(operator, answers, request)
      }

      def theRequestBuilt: JourneyRequest[_] = {
        val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[_]])
        verify(block).apply(captor.capture())
        captor.getValue
      }
    }

    "block request" when {
      "answers not found" in {
        given(movementRepository.findByPid("pid")).willReturn(Future.successful(None))

        await(refiner.invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayChoiceForm())
      }
    }
  }

}
