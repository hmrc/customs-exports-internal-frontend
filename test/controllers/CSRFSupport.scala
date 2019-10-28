package controllers

import play.api.mvc.Request
import play.api.test.{CSRFTokenHelper, FakeRequest}

trait CSRFSupport {
  implicit class CSRFFakeRequest[A](request: FakeRequest[A]) {
    def withCSRFToken: Request[A] = CSRFTokenHelper.addCSRFToken(request)
  }
}
