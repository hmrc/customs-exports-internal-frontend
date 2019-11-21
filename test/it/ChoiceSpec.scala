import forms.Choice
import models.cache.{Cache, DepartureAnswers}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers._
import reactivemongo.play.json.ImplicitBSONHandlers._

import scala.concurrent.ExecutionContext.Implicits.global

class ChoiceSpec extends IntegrationSpec {

  "Display Page" should {
    "return 403" in {
      givenAuthFailed()

      val response = get(controllers.routes.ChoiceController.displayPage())

      status(response) mustBe Status.FORBIDDEN
    }

    "return 200" in {
      givenAuthSuccess()

      val response = get(controllers.routes.ChoiceController.displayPage())

      status(response) mustBe Status.OK
    }
  }

  "Submit" should {
    "return 403" in {
      givenAuthFailed()

      val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.Departure.value)

      status(response) mustBe Status.FORBIDDEN
    }

    "return 200" in {
      givenAuthSuccess("pid")

      val response = post(controllers.routes.ChoiceController.submit(), "choice" -> Choice.Departure.value)

      status(response) mustBe Status.SEE_OTHER
      await(cache.find(Json.obj("providerId" -> "pid")).one[Cache]) mustBe Some(Cache("pid", DepartureAnswers()))
    }
  }
}
