name    := "scalr"

version := "0.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
   "org.erlang.otp" % "jinterface" % "1.5.6",
   "com.typesafe.akka" %% "akka-actor" % "2.3.14",
   "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

lazy val root = (project in file(".")).
  settings(
    javaOptions ++= Seq(
      "-Xmx1G"
    )
  )


