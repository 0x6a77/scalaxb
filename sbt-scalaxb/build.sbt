sbtPlugin := true

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.1")

publishMavenStyle := true

seq(ScriptedPlugin.scriptedSettings: _*)

scriptedBufferLog := false
