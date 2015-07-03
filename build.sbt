organization := "com.evojam"

name := "mongo-driver-scala"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6")

scalacOptions ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

resolvers ++= Seq(
  "Maven Central Mirror UK" at "http://uk.maven.org/maven2",
  Resolver.sbtPluginRepo("snapshots"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.typesafeRepo("releases")
)

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.specs2" %% "specs2-core" % "2.4.11" % "test",
  "org.specs2" %% "specs2-junit" % "2.4.11" % "test",
  "org.specs2" %% "specs2-mock" % "2.4.11" % "test",
  "io.netty" % "netty-all" % "4.0.28.Final",
  "org.mongodb" % "mongodb-driver-core" % "3.0.1",
  "io.reactivex" % "rxscala_2.11" % "0.25.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)