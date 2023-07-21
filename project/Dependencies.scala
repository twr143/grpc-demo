import sbt._
import Keys._
import scala.util.Try

import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}


object Dependencies {
  
  val doobieVersion = "1.0.0-RC1"
  val http4sVersion = "0.23.18"
  val circeVersion = "0.14.1"
  val tsecVersion = "0.4.0"
  val sttpVersion = "3.3.18"
  val prometheusVersion = "0.14.1"
  val tapirVersion = "0.20.0-M3"
  val fs2Version = "2.5.10"
  val catsEffectVersion = "3.3.14"

  val dbDependencies = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.flywaydb" % "flyway-core" % "6.2.1"
  )
  val rpcDependencies = Seq(
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
  )
  val httpDependencies = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-prometheus-metrics" % "0.23.12",
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpVersion,
    ("com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion).cross(CrossVersion.for3Use2_13)
  ).map(_.withSources())

  val monitoringDependencies = Seq(
    "io.prometheus" % "simpleclient" % prometheusVersion,
    "io.prometheus" % "simpleclient_hotspot" % prometheusVersion,
    ("com.softwaremill.sttp.client3" %% "prometheus-backend" % sttpVersion).cross(CrossVersion.for3Use2_13),
    "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion
  ).map(_.withSources())

  val jsonDependencies = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    ("io.circe" %% "circe-generic-extras" % circeVersion).cross(CrossVersion.for3Use2_13),
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
    ("com.softwaremill.sttp.client3" %% "circe" % sttpVersion).cross(CrossVersion.for3Use2_13)
  ).map(_.withSources())

  val loggingDependencies = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "org.codehaus.janino" % "janino" % "3.1.9",
    "de.siegmar" % "logback-gelf" % "4.0.2"
  )

  val configDependencies = Seq(
    ("com.github.pureconfig" %% "pureconfig" % "0.17.2").cross(CrossVersion.for3Use2_13)
  )

  val baseDependencies = Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.softwaremill.common" %% "tagging" % "2.3.4",
    "com.softwaremill.quicklens" %% "quicklens" % "1.8.10"
  )

  val apiDocsDependencies = Seq("com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion)
  val scalatest = "org.scalatest" %% "scalatest" % "3.2.15" % Test
  val unitTestingStack = Seq(scalatest)

  val commonDependencies = baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies

  val apiDependencies: Seq[ModuleID] = commonDependencies ++ httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ rpcDependencies

  val coreDependencies: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
    "com.typesafe" % "config" % "1.4.2",
  )

  val MyAppDependencies: Seq[ModuleID] = Seq(
  )

  lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    action("lastCommitHash") {
      import scala.sys.process._
      // if the build is done outside of a git repository, we still want it to succeed
      Try("git rev-parse HEAD".!!.trim).getOrElse("?")
    }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime,
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.ToMap,
  buildInfoPackage := "demo3tier.api.version",
)
  val scalacOptionsS = List(
    "-encoding",
    "utf8",
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Wunused",
    "-Wdead-code",
    "-Ymacro-annotations"
  )
  val domainDeps = Seq("com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf")


}
