import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo"          % "7.20.0-play-26",
    "uk.gov.hmrc" %% "govuk-template"                % "5.42.0-play-26",
    "uk.gov.hmrc" %% "play-json-union-formatter"     % "1.5.0",
    "uk.gov.hmrc" %% "play-ui"                       % "8.2.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26"             % "1.1.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.1.0-play-26"
  ).map(_.withSources())

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.1.0" % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test",
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.mockito"             %  "mockito-core"             % "3.0.0"                 % "test",
    "com.github.tomakehurst"  % "wiremock-jre8"             % "2.24.1"                % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it"
  ).map(_.withSources())

}
