import sbt._
import java.io.{File, InputStream, FileWriter}

trait VersionFileTask extends DefaultProject {
  def versionFilePackage = "foo"
  def versionFilePath = path("src") / "main" / "scala" / versionFilePackage
  def versionFileName = "Version.scala"
  
  def versionFileContent = """package %s
    |
    |trait Version { 
    |  val version = "%s" 
    |}
    |""".stripMargin.format(versionFilePackage, version)
  
  lazy val versionfile = task {
    val fullPath = versionFilePath / versionFileName
    val content = versionFileContent

    if (!fullPath.asFile.exists) FileUtilities.write(fullPath.asFile, content, log)
    else FileUtilities.readString(fullPath.asFile, log) match {
        case Right(`content`) => // do nothing
        case _ => FileUtilities.write(fullPath.asFile, content, log)
      }
    None
  }
}
