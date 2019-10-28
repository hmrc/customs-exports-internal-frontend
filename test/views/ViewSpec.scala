package views

import controllers.CSRFSupport
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.twirl.api.Html

class ViewSpec extends WordSpec with MustMatchers with ViewTemplates with ViewMatchers with CSRFSupport {

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  private val injector = new GuiceApplicationBuilder().injector()
  private val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  protected implicit def messages(implicit request: Request[_]): Messages = messagesApi.preferred(request)
  protected implicit def messages(key: String)(implicit request: Request[_]): String = messages(request)(key)

  protected implicit class CommonElementFinder(html: Html) {
    private val document = htmlBodyOf(html)

    def getTitle: Element = document.getElementsByTag("title").first()

    def getBackButton: Element = document.getElementById("link-back")

    def getErrorSummary: Option[Element] = Option(document.getElementById("error-summary"))
  }

}
