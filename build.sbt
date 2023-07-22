import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "org.iv"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val api = (project in file("api")).
  dependsOn(domain).
  enablePlugins(BuildInfoPlugin).
  settings(name := "grpc-demo-api").
  settings(scalacOptions ++= scalacOptionsS).
  settings(buildInfoSettings).
  settings(libraryDependencies ++= apiDependencies).
  settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full))

lazy val core = (project in file("core")).
  dependsOn(domain).
  settings(
    name := "grpc-demo-core",
    libraryDependencies ++= coreDependencies)

lazy val domain = (project in file("domain")).
  enablePlugins(Fs2Grpc).
  settings(
    name := "grpc-demo-domain",
    scalapbCodeGeneratorOptions += CodeGeneratorOption.Fs2Grpc,
  ).settings(libraryDependencies ++= domainDeps)

lazy val root = (project in file("."))
  .settings(
    name := "aggregate",
  ).aggregate(domain, api, core)

addCommandAlias("ci", ";clean;compile")