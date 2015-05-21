scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:postfixOps"
)

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0-RC1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
