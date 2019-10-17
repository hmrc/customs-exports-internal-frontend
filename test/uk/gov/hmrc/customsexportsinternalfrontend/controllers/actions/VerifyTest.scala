package uk.gov.hmrc.customsexportsinternalfrontend.controllers.actions

import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

class VerifyTest extends WordSpec with MustMatchers with MockitoSugar {

  "authenticated" should {
    "delegate to action" in {
      val action = mock[AuthenticatedAction]

      new Verify(action).authenticated mustBe action
    }
  }

}
