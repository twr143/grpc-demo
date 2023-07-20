import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val api = (project in file("api")).
  dependsOn(domain).
  settings(libraryDependencies ++= apiDependencies)

lazy val core = (project in file("core")).
  dependsOn(domain).
  settings(libraryDependencies ++= coreDependencies)

lazy val domain = (project in file("domain")).
  enablePlugins(Fs2Grpc).
  settings(
    scalapbCodeGeneratorOptions += CodeGeneratorOption.Fs2Grpc
  )
  configs(Test)
