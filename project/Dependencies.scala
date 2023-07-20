import sbt._
import Keys._

object Dependencies {

  lazy val version = new {
      val scalaTest       = "3.0.0"
      val scalaCheck      = "1.13.4"
  }

 
  val apiDependencies: Seq[ModuleID] = Seq(
  )

  val coreDependencies: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
    "com.typesafe" % "config" % "1.4.2",
  )

  val MyAppDependencies: Seq[ModuleID] = Seq(
  )

}
