import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "customs-exports-internal-frontend"

PlayKeys.devSettings := List("play.server.http.port" -> "6799")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Test / unmanagedSourceDirectories := List((Test / baseDirectory).value / "test/unit", (Test / baseDirectory).value / "test/util"),
    addTestReportOption(Test, "test-reports")
  )
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := List(
      (IntegrationTest / baseDirectory).value / "test/it",
      (Test / baseDirectory).value / "test/util"
    ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false
  )
  .settings(jsSettings)
  .settings(scoverageSettings)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val commonSettings = List(
  majorVersion := 0,
  scalaVersion := "2.13.8",
  scalacOptions ++= scalacFlags,
  retrieveManaged := true,
  dependencyOverrides += "commons-codec" % "commons-codec" % "1.15",
  libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
  TwirlKeys.templateImports ++= List.empty
)

lazy val scalacFlags = List(
  "-deprecation",                                // warn about use of deprecated APIs
  "-encoding", "UTF-8",                          // source files are in UTF-8
  "-feature",                                    // warn about misused language features
  "-language:implicitConversions",
  "-unchecked",                                  // warn about unchecked type parameters
  //"-Wconf:any:warning-verbose",
  "-Wconf:cat=unused-imports&src=routes/.*:s",   // silent "unused import" warnings from Play routes
  "-Wconf:cat=unused-imports&src=twirl/.*:is",   // silent "unused import" warnings from Twirl templates
  "-Wextra-implicit",
  "-Xcheckinit",
  "-Xfatal-warnings",                            // warnings are fatal!!
  "-Ywarn-numeric-widen",
)

lazy val jsSettings = List(
  uglifyCompressOptions := List("unused=false", "dead_code=false"),
  pipelineStages := List(digest),
  // below line required to force asset pipeline to operate in dev rather than only prod
  Assets / pipelineStages := List(concat, uglify),
  // only compress files generated by concat
  uglify / includeFilter := GlobFilter("customsdecexfrontend-*.js")
)

lazy val scoverageSettings = List(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*",
    "logger.*\\(.*\\)"
  ).mkString(";"),
  coverageMinimumStmtTotal := 80,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)
