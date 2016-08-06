organization := "org.zalando"

name    := "scarl"

version := "0.1.3"

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

//
//
pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/zalando/scarl</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/zalando/scarl</url>
    <connection>scm:git:git@github.com:zalando//scarl.git</connection>
  </scm>
  <developers>
    <developer>
      <name>Dmitry Kolesnikov</name>
      <email>dmitry.kolesnikov@zalando.fi</email>
      <organization>Zalando SE</organization>
    </developer>
  </developers>)



