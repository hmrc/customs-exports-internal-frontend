import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo"          % "7.21.0-play-26",
    "uk.gov.hmrc" %% "govuk-template"                % "5.42.0-play-26",
    "uk.gov.hmrc" %% "play-json-union-formatter"     % "1.5.0",
    "uk.gov.hmrc" %% "play-ui"                       % "8.5.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26"             % "1.1.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.1.0-play-26",
    "com.github.tototoshi" %% "scala-csv" % "1.3.6"
  ).map(_.withSources())

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.1.0" % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.mockito"             %  "mockito-core"             % "3.0.0"                 % "test, it",
    "com.github.tomakehurst"  % "wiremock-jre8"             % "2.24.1"                % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it",
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.15.0-play-26"        % "test, it"
  ).map(_.withSources())

}
