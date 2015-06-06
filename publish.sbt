publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/evojam/mongodb-driver-scala</url>
    <licenses>
      <license>
        <name>Apache 2.0 License</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:evojam/mongodb-driver-scala.git</url>
      <connection>scm:git:git@github.com:evojam/mongodb-driver-scala.git</connection>
    </scm>
    <developers>
      <developer>
        <id>dumpstate</id>
        <name>Albert Sadowski</name>
      </developer>
      <developer>
        <id>duketon</id>
        <name>Michael Kendra</name>
        <url>http://michaelkendra.me/</url>
      </developer>
      <developer>
        <id>abankowski</id>
        <name>Artur Bańkowski</name>
      </developer>
    </developers>)

licenses := Seq("Apache 2.0 License" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/evojam/mongodb-driver-scala"))