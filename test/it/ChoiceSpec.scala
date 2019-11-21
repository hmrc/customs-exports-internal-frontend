import forms.Choice
import play.api.http.Status
import play.api.libs.ws.WSResponse

class ChoiceSpec extends IntegrationSpec {

  "Display Page" should {
    "return 403" in {
      givenAuthFailed()

      val response: WSResponse = get(controllers.routes.ChoiceController.displayPage())

      response.status mustBe Status.FORBIDDEN
    }

    "return 200" in {
      givenAuthSuccess()

      val response: WSResponse = get(controllers.routes.ChoiceController.displayPage())

      response.status mustBe Status.OK
    }
  }

  "Submit" should {
    "return 403" in {
      givenAuthFailed()

      val response: WSResponse = post(controllers.routes.ChoiceController.submit(), Choice.Departure)

      response.status mustBe Status.FORBIDDEN
    }

    "return 200" in {
      givenAuthSuccess()

      val response: WSResponse = post(controllers.routes.ChoiceController.submit(), Choice.Departure)

      response.status mustBe Status.OK
    }
  }
}
