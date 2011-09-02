package sbtscalaxb

import sbt._
import scalaxb.{compiler => sc}

object Plugin extends sbt.Plugin {
  import Keys._
  import Project.Initialize

  val Scalaxb          = config("scalaxb") extend(Compile)
  val scalaxb          = TaskKey[Seq[File]]("scalaxb")
  val generate         = TaskKey[Seq[File]]("generate")
  val xsdSource        = SettingKey[File]("xsd-source")
  val packageName      = SettingKey[String]("package-name")
  val packageNames     = SettingKey[Map[URI, String]]("package-names")
  val classPrefix      = SettingKey[Option[String]]("class-prefix")
  val paramPrefix      = SettingKey[Option[String]]("param-prefix")
  val wrapContents     = SettingKey[Seq[String]]("wrap-contents")
  val chunkSize        = SettingKey[Int]("chunk-size")
  val packageDir       = SettingKey[Boolean]("package-dir")
  val generateRuntime  = SettingKey[Boolean]("generate-runtime")

  val scalaxbConfig    = SettingKey[sc.Config]("scalaxb-config")
  val combinedPackageNames = SettingKey[Map[Option[String], Option[String]]]("combined-package-names")

  def generateTask(base: File, config: sc.Config, sources: Seq[File], ll: Level.Value): Seq[File] =
    sources.headOption map { src =>
      import sc._
      val module = Module.moduleByFileName(src, ll == Level.Debug)
      module.processFiles(sources, config.copy(outdir = base))
    } getOrElse {Nil}

  def cleanTask(base: File) {
    IO.delete((base ** "*").get)
    IO.createDirectory(base)
  }

  def combinedPackageNamesSetting: Initialize[Map[Option[String], Option[String]]] =
    (packageName, packageNames) { (x, xs) =>
      (xs map { case (k, v) => ((Some(k.toString): Option[String]), Some(v)) }) updated (None, Some(x))
    }

  def scalaxbConfigSetting: Initialize[sc.Config] =
    (combinedPackageNames, packageDir, classPrefix, paramPrefix,
     wrapContents, generateRuntime, chunkSize) {
      (pkg, pkgdir, cpre, ppre, w, rt, cs) =>
      sc.Config(packageNames = pkg,
        packageDir = pkgdir,
        classPrefix = cpre,
        paramPrefix = ppre,
        wrappedComplexTypes = w.toList,
        generateRuntime = rt,
        sequenceChunkSize = cs
      )
    }

  lazy val scalaxbSettings: Seq[Project.Setting[_]] = inConfig(Scalaxb)(Seq(
    generate <<= (sourceManaged, scalaxbConfig, sources, logLevel, clean) map { (base, config, sources, ll, _) =>
      generateTask(base, config, sources, ll) },
    sourceManaged <<= (sourceManaged in Compile).identity,
    sources <<= (xsdSource) map { xsd => (xsd ** "*.xsd").get },
    xsdSource <<= (sourceDirectory) { src => src / "main" / "xsd" },
    clean <<= (sourceManaged) map { (base) => cleanTask(base) },
    packageName := "generated",
    packageNames := Map(),
    classPrefix := None,
    paramPrefix := None,
    wrapContents := Nil,
    chunkSize := 10,
    packageDir := true,
    generateRuntime := true,
    combinedPackageNames <<= combinedPackageNamesSetting,
    scalaxbConfig <<= scalaxbConfigSetting,
    logLevel <<= (logLevel in Compile).identity
  )) ++
  Seq(
    scalaxb <<= (generate in Scalaxb) map { x => x }
  )
}
