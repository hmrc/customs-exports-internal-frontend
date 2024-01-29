import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.3.0"
  val hmrcMongoVersion = "1.7.0"
  val jacksonVersion = "2.14.2"

  val compile = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"               % bootstrapPlayVersion,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"               % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"                       % hmrcMongoVersion,
    // Used by the Migration tool. Keep this library's version to the same major.minor version as the mongo-scala-driver.
    "org.mongodb"                   % "mongodb-driver-sync"                       % "4.11.1",
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"    % "2.0.0",
    "uk.gov.hmrc"                   %% "play-json-union-formatter"                % "1.20.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                     % jacksonVersion,
    "com.github.tototoshi"          %% "scala-csv"                                % "1.3.10",
    "org.webjars.npm"               %  "accessible-autocomplete"                  % "2.0.4"
  ).map(_.withSources)

  val testScope = "test,it"

  val test = Seq(
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"       % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-30"      % hmrcMongoVersion     % testScope,
    "com.vladsch.flexmark"       %  "flexmark-all"                 % "0.64.6"             % testScope,
    "org.jsoup"                  %  "jsoup"                        % "1.15.4"             % "test",
    "org.mockito"                %% "mockito-scala-scalatest"      % "1.17.29"            % testScope,
    "org.scalatest"              %% "scalatest"                    % "3.2.15"             % testScope,
  )
}
