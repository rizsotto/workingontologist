import Dependencies._

scalaVersion := "3.6.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.rizsotto",
      scalaVersion := "2.13.15",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "workingontologist",
    libraryDependencies ++= Seq(
      jena,
      scalaTest % Test
    )
  )
