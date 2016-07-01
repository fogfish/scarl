name    := "scarl"

version := "0.1.0"

scalaVersion := "2.11.8"

//
//
lazy val akkaVersion = "2.4.7"
libraryDependencies ++= Seq(
   "org.erlang.otp" % "jinterface" % "1.5.6",
   "com.typesafe.akka" %% "akka-actor" % akkaVersion,
   "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

lazy val root = (project in file(".")).
  settings(
    javaOptions ++= Seq(
      "-Xmx1G"
    )
  )


