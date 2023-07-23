import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "org.iv"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val api = (project in file("api")).
  dependsOn(domain).
  enablePlugins(BuildInfoPlugin, DockerPlugin).
  settings(name := "grpc-demo-api").
  settings(scalacOptions ++= scalacOptionsS).
  settings(buildInfoSettings).
  settings(libraryDependencies ++= apiDependencies).
  settings(docker / dockerfile := dockerFile(
    jarFile = (Compile / packageBin / sbt.Keys.`package`).value,
    classpath = (Compile / managedClasspath).value,
    mainclass = (Compile / packageBin / mainClass).value.getOrElse(sys.error("Expected exactly one main class")),
    rsc = (Compile / resourceDirectory).value
  )).
  settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full))

lazy val core = (project in file("core")).
  dependsOn(domain).
  enablePlugins(DockerPlugin).
  settings(
    name := "grpc-demo-core",
    libraryDependencies ++= coreDependencies,
  ).
  settings(docker / dockerfile := dockerFile(
  jarFile = (Compile / packageBin / sbt.Keys.`package`).value,
  classpath = (Compile / managedClasspath).value,
  mainclass = (Compile / packageBin / mainClass).value.getOrElse(sys.error("Expected exactly one main class")),
  rsc = (Compile / resourceDirectory).value
))

lazy val domain = (project in file("domain")).
  enablePlugins(Fs2Grpc).
  settings(
    name := "grpc-demo-domain",
    scalapbCodeGeneratorOptions += CodeGeneratorOption.Fs2Grpc,
  ).settings(libraryDependencies ++= domainDeps)

lazy val root = (project in file("."))
  .settings(
    name := "aggregate",
  ).aggregate(api, core)

addCommandAlias("ci", ";clean;compile")

def dockerFile(jarFile: File, classpath: Classpath, mainclass: String, rsc: File) = {
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  val vmArgs = "-XX:+UnlockExperimentalVMOptions -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+UseContainerSupport -XX:MinRAMPercentage=50.0 -XX:MaxRAMPercentage=95.0".split(" ")
  new Dockerfile {
    // Base image
    from("openjdk:8-jre-slim")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", vmArgs(0), vmArgs(1), vmArgs(2), vmArgs(3), vmArgs(4), vmArgs(5), "-cp", classpathString, mainclass)
  }
  // дополнительно после push:
  // minikube image load com.iv/cats3-grpc
  // minikube image ls --format table
  // kubectl apply -f deployment.yaml
  // kubectl exec -it <Pod_Name>  -- /bin/bash  /sh
}