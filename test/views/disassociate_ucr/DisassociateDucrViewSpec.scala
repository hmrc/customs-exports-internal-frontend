package views.disassociate_ucr

import forms.DisassociateDucr
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociate_ducr

class DisassociateDucrViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken
  private val page = new disassociate_ducr(main_template)

  "View" should {
    "render title" in {
      page(DisassociateDucr.form).getTitle must containMessage("disassociateDucr.title")
    }

    "render back button" in {
      page(DisassociateDucr.form).getBackButton must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "render error summary" when {
      "no errors" in {
        page(DisassociateDucr.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(DisassociateDucr.form.withError("error", "message")).getErrorSummary mustBe defined
      }
    }
  }

}
