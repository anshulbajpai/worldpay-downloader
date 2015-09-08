import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._

  import TestPhases._

  val appName: String
  val appDependencies : Seq[ModuleID]

  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  def unitFilter(name: String): Boolean = !funFilter(name)
  def funFilter(name: String): Boolean = name endsWith "FeatureSuite"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.PlayScala) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      retrieveManaged := true
    )
    .settings(testOptions in Test := Seq(Tests.Filter(unitFilter), Tests.Argument("-l", "Vagrant")))
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings) : _*)
    .settings(
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      Keys.fork in IntegrationTest := false,
      parallelExecution in IntegrationTest := false
    )
    .configs(FunTest)
    .settings(inConfig(FunTest)(Defaults.testSettings): _*)
    .settings(
      testOptions in FunTest := Seq(Tests.Filter(funFilter)),
      unmanagedSourceDirectories   in FunTest <<= (baseDirectory in FunTest)(base => Seq(base / "test")),
      unmanagedResourceDirectories in FunTest <<= (baseDirectory in FunTest)(base => Seq(base / "test"))
    )
    .settings(
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      )
    )
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
}

private object TestPhases {

  lazy val FunTest = config("fun") extend Test

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
