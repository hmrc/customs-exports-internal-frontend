import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"    % "5.12.0",
    "uk.gov.hmrc"          %% "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc"          %% "govuk-template"                % "5.69.0-play-28",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.9.0-play-28",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"            % "0.72.0-play-28",
    "uk.gov.hmrc"          %%  "simple-reactivemongo"         % "8.0.0-play-28",
    "uk.gov.hmrc"          %% "play-json-union-formatter"     % "1.13.0-play-27",
    "ai.x"                 %% "play-json-extensions"          % "0.42.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala" % "2.12.3",
    "com.github.tototoshi" %% "scala-csv"                     % "1.3.8",
    "com.github.cloudyrock.mongock" %  "mongock-core"         % "2.0.2",
    "org.mongodb"          %  "mongo-java-driver"             % "3.12.8",
    "org.webjars.npm"      %  "govuk-frontend"                % "3.12.0",
    "org.webjars.npm"      %  "accessible-autocomplete"       % "2.0.3"
  ).map(_.withSources)

  val test = Seq(
    "com.typesafe.play"       %% "play-test"                % current          % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.2.9"          % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"          % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8"         % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.13.1"         % "test, it",
    "org.scalatestplus"       %% "mockito-3-4"              % "3.2.9.0"        % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.28.1"         % "test, it"
  ).map(moduleID => if (moduleID.name.contains("flexmark")) moduleID else moduleID.withSources)
}
