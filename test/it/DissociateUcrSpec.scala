import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, verify}
import forms.{DisassociateKind, DisassociateUcr}
import models.cache.DisassociateUcrAnswers
import play.api.test.Helpers._

class DissociateUcrSpec extends IntegrationSpec {

  "Dissociate UCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", DisassociateUcrAnswers())

        val response = get(controllers.consolidations.routes.DisassociateUCRController.display())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor("pid", DisassociateUcrAnswers())

        val response = post(controllers.consolidations.routes.DisassociateUCRController.submit(), "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.DisassociateUCRSummaryController.display().url)
        theCacheFor("pid") mustBe Some(
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )
      }
    }
  }

  "Dissociate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )

        val response = get(controllers.consolidations.routes.DisassociateUCRSummaryController.display())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("pid")
        givenCacheFor(
          "pid",
          DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = DisassociateKind.Mucr, mucr = Some("GB/321-54321"), ducr = None)))
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(controllers.consolidations.routes.DisassociateUCRSummaryController.submit())

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.DisassociateUCRConfirmationController.display().url)
        theCacheFor("pid") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(
              equalTo(
                """{"providerId":"pid","eori":"GB1234567890","ucr":"GB/321-54321","consolidationType":"DISASSOCIATE_DUCR"}"""
              )
            )
        )
      }
    }
  }
}
