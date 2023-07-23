import sbt._
import Keys._


class CustomDocker extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    val dkr = taskKey[Unit]("create a dockerfile for all subprojects")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    dkr := {
      val n = name.value
      println(s"uploading $n..")
    }
  )
}
