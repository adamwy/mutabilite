import sbt.Keys._

name := "mutabilite"

lazy val defaults = Defaults.coreDefaultSettings ++ Seq(
  organization := "mutabilite",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.1",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1"
)

lazy val core = project
  .in(file("core"))
  .settings(
    defaults ++ Seq(
      moduleName := "core",
      libraryDependencies ++= testDependencies
    )
  )

lazy val macros = project
  .in(file("macros"))
  .dependsOn(core)
  .settings(
    defaults ++ Seq(
      moduleName := "macros",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.12.1"
      )
    )
  )

lazy val ops = project
  .in(file("ops"))
  .dependsOn(macros)
  .settings(
    defaults ++ Seq(
      moduleName := "ops",
      libraryDependencies ++= testDependencies
    )
)

lazy val benchmark = project
  .in(file("benchmark"))
  .dependsOn(ops)
  .enablePlugins(JmhPlugin)
  .settings(
    defaults ++ Seq(
      moduleName := "benchmark",
      resolvers += Resolver.sonatypeRepo("releases"),
      libraryDependencies ++= Seq(
//        "org.spire-math" %% "debox" % "0.7.3"
      )
    )
  )

lazy val readme =
  scalatex.ScalatexReadme(
    projectId = "readme",
    wd = file(""),
    url = "https://github.com/lihaoyi/scalatex/tree/master",
    source = "Readme"
  )

