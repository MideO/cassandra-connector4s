name := "cassandra-connector"

organization := "com.github.mideo"

lazy val `cassandra-connector` = (project in file("."))
  .settings(
    scalacOptions := Seq(
      "-unchecked",
      "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-Yrangepos"
    )
  )

fork in run := true

parallelExecution in `cassandra-connector` := false

resolvers ++= Seq(
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  "Sonatypes" at "https://oss.sonatype.org/content/repositories/releases",
  "Maven Repo" at "http://mvnrepository.com/maven2/"
)

scalaVersion := "2.12.6"

resolvers += Classpaths.typesafeReleases
resolvers += JCenterRepository

libraryDependencies ++= Seq(
  "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.6.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "net.databinder.dispatch" %% "dispatch-core" % "0.13.2",
  "com.typesafe" % "config" % "1.3.3",
  "org.apache.cassandra" % "cassandra-all" % "3.11.3" exclude("ch.qos.logback", "logback-classic") exclude("org.slf4j", "slf4j-log4j12"),
  "org.cassandraunit" % "cassandra-unit" % "3.5.0.1",
  "uk.sky" % "cqlmigrate" % "0.9.8" exclude("org.slf4j", "slf4j-api"),
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test


)


pomIncludeRepository := { _ => true }

publishMavenStyle := true

publishArtifact in Test := false

val oss_user = if (sys.env.keySet.contains("OSS_USERNAME")) sys.env("OSS_USERNAME") else ""
val oss_pass = if (sys.env.keySet.contains("OSS_PASSWORD")) sys.env("OSS_PASSWORD") else ""
val gpg_pass = if (sys.env.keySet.contains("GPG_PASSWORD")) sys.env("GPG_PASSWORD").toCharArray else Array.emptyCharArray

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org", oss_user, oss_pass)

pgpPassphrase := Some(gpg_pass)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/MideO/sssh"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/MideO/sssh"),
    "scm:git@github.com/MideO/sssh"
  )
)

developers := List(
  Developer(
    id = "mideo",
    name = "Mide Ojikutu",
    email = "mide.ojikutu@gmail.com",
    url = url("https://github.com/MideO")
  )
)

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}
val tagOrHash = Def.setting {
  if (isSnapshot.value)
    sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else
    tagName.value
}


// Release
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseVersionBump := sbtrelease.Version.Bump.Next

releaseIgnoreUntrackedFiles := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)