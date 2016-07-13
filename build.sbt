import sbt.Keys._

name := "scala-offheap-collections"

lazy val defaults = Defaults.coreDefaultSettings ++ Seq(
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.8",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val api = project
  .in(file("api"))
  .settings(
    defaults ++ Seq(
      moduleName := "api"
    )
  )

lazy val tests = project
  .in(file("tests"))
  .dependsOn(api)
  .settings(
    defaults ++ Seq(
      moduleName := "tests",
      libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.11" % "2.2.4",
        "org.scalacheck" %% "scalacheck" % "1.12.2"
      )
    )
  )

lazy val naive = project
  .in(file("naive"))
  .dependsOn(api, tests % "test")
  .settings(
    defaults ++ Seq(
      moduleName := "naive"
    )
  )

lazy val generic = project
  .in(file("generic"))
  .dependsOn(api, tests % "test")
  .settings(
    defaults ++ Seq(
      moduleName := "generic"
    )
  )

lazy val specializedCore = project
  .in(file("specialized-core"))
  .dependsOn(api)
  .settings(
    defaults ++ Seq(
      moduleName := "specialized-core"
    )
  )

lazy val macros = project
  .in(file("macros"))
  .dependsOn(specializedCore)
  .settings(
    defaults ++ Seq(
      moduleName := "macros",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.11.8"
      )
    )
  )

lazy val specialized = project
  .in(file("specialized"))
  .dependsOn(api, macros, tests % "test")
  .settings(
    defaults ++ Seq(
      moduleName := "specialized"
    )
)

lazy val offheap = project
  .in(file("offheap"))
  .dependsOn(api, specialized, tests % "test")
  .settings(
    defaults ++ Seq(
      moduleName := "offheap",
      libraryDependencies ++= Seq(
        "sh.den" % "scala-offheap_2.11" % "0.2-SNAPSHOT"
      )
    )
  )

lazy val benchmark = project
  .in(file("benchmark"))
  .dependsOn(generic, specialized, offheap)
  .enablePlugins(JmhPlugin)
  .settings(
    defaults ++ Seq(
      moduleName := "benchmark"
    )
  )
