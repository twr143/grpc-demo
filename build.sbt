import Dependencies._
import scala.util.Try

Global / onChangedBuildSource := ReloadOnSourceChanges


lazy val api = (project in file("api")).
  dependsOn(domain).
  enablePlugins(BuildInfoPlugin).
  settings(scalaVersion := "2.13.10").
  settings(scalacOptions ++= scalacOptionsS).
  settings(buildInfoSettings).
  settings(libraryDependencies ++= apiDependencies).
  settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full))

lazy val core = (project in file("core")).
  dependsOn(domain).
  settings(scalaVersion := "2.13.10").
  settings(libraryDependencies ++= coreDependencies)

lazy val domain = (project in file("domain")).
  enablePlugins(Fs2Grpc).
  settings(
    scalapbCodeGeneratorOptions += CodeGeneratorOption.Fs2Grpc,
    scalaVersion := "2.13.10"
  )
  configs(Test)
