package controllers

import forms.FindCdsUcr
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.Helpers.status
import services.{MockCache, MockIleQueryCache}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.find_cds_consignment

import scala.concurrent.ExecutionContext.global

class FindCdsConsignmentControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache with ScalaFutures with IntegrationPatience {
  private val findCdsConsignmentPage = mock[find_cds_consignment]

  private def controller = new FindCdsConsignmentController(
    stubMessagesControllerComponents(),
    SuccessfulAuth(),
    cacheRepository,
    findCdsConsignmentPage,
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(findCdsConsignmentPage)
    when(findCdsConsignmentPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(findCdsConsignmentPage)
    super.afterEach()
  }

  "FindCdsConsignmentController on displayPage" should {
    "return Ok (200) response" in {
      givenTheCacheIsEmpty()
      val result = controller.displayPage(getRequest)
      status(result) mustBe OK
    }
  }

  "provided with semantically incorrect UCR" should {
    val incorrectUCR = "123ABC-789456POIUYT"

    "return Find CDS Consignment page, passing form with errors" in {

      // controller.getConsignmentInformation(incorrectUCR)(request).futureValue

      val expectedForm = FindCdsUcr.form.fillAndValidate(FindCdsUcr(incorrectUCR))
      verify(findCdsConsignmentPage).apply(meq(expectedForm))(any(), any())
    }
  }

}