name := "akka-streams-intro"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.16",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.hunorkovacs" %% "koauth" % "1.1.0",
  "io.circe" %% "circe-generic" % "0.7.0",
  "io.circe" %% "circe-parser" % "0.7.0",
  "com.typesafe" % "config" % "1.3.1",

  "org.apache.lucene" % "lucene-analyzers-common" % "6.4.0",

  "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.5"
)