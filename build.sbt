import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.rizsotto.maize",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Stalk",
    libraryDependencies ++= Seq(
      jena,
      scalaTest % Test
    )
  )
