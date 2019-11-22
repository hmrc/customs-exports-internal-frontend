import forms.ShutMucr
import models.cache.ShutMucrAnswers
import play.api.test.Helpers._
import com.github.tomakehurst.wiremock.client.WireMock.{verify, equalTo}

class ShutMucrSpec extends IntegrationSpec {

  "Enter MUCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers())

        val response = get(controllers.consolidations.routes.ShutMucrController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers())

        val response = post(controllers.consolidations.routes.ShutMucrController.submit(), "mucr" -> "GB/123-12345")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMucrSummaryController.displayPage().url)
        theCacheFor("pid") mustBe Some(ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
      }
    }
  }

  "Shut MUCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))

        val response = get(controllers.consolidations.routes.ShutMucrSummaryController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(controllers.consolidations.routes.ShutMucrSummaryController.submit())

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMUCRConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalTo("""{"providerId":"pid","eori":"GB1234567890","mucr":"GB/123-12345","consolidationType":"SHUT_MUCR"}"""))
        )
      }
    }
  }
}
