import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  override val appName = "worldpay-downloader"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(

    "uk.gov.hmrc" %% "play-health" % "1.1.0",
    "uk.gov.hmrc" %% "microservice-bootstrap" % "3.0.0",
    "uk.gov.hmrc" %% "play-config" % "2.0.1",
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.0",
    "uk.gov.hmrc" %% "order-id-encoder" % "0.13.0",

    "org.apache.httpcomponents" % "httpclient" % "4.3.4",
    "org.apache.sshd" % "sshd-core" % "0.12.0",
    "org.apache.sshd" % "sshd-sftp" % "0.11.0",
    "com.jcraft" % "jsch" % "0.1.51",
    "org.apache.commons" % "commons-vfs2" % "2.0"
  )

  abstract class TestDependencies(scope: String) {
    lazy val test : Seq[ModuleID] = Seq(
      "org.scalacheck" %% "scalacheck" % "1.12.1" % scope,
      "org.scalatest" %% "scalatest" % "2.2.0" % scope,
      "org.scalatestplus" %% "play" % "1.2.0" % scope,
      "org.pegdown" % "pegdown" % "1.4.2" % scope,
      "uk.gov.hmrc" %% "hmrctest" % "1.2.0" % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "info.cukes" %% "cucumber-scala" % "1.1.8" % scope,
      "info.cukes" % "cucumber-junit" % "1.1.8" % scope,
      "com.github.tomakehurst" % "wiremock" % "1.57" % scope
    )
  }

  object Test extends TestDependencies("test")
  object IntegrationTest extends TestDependencies("it")

  def apply() = compile ++ Test.test ++ IntegrationTest.test
}
