import scalariform.formatter.preferences._

name := """feedfrenzy-scala"""

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.github.tototoshi"    %% "slick-joda-mapper"        % "1.2.0",
  "com.typesafe.akka"       %% "akka-stream-experimental" % "1.0-M2",
  "com.typesafe.slick"      %% "slick"                    % "2.1.0",
  "joda-time"                % "joda-time"                % "2.4",
  "net.databinder.dispatch"  % "dispatch-core_2.11"       % "0.11.1",
  "org.joda"                 % "joda-convert"             % "1.6",
  "org.scalaj"              %% "scalaj-http"              % "0.3.16",
  "org.scalatest"           %% "scalatest"                % "2.1.6"             % "test",
  "org.slf4j"                % "slf4j-nop"                % "1.6.4",
  "org.specs2"               % "specs2_2.11"              % "2.4",
  "postgresql"               % "postgresql"               % "9.1-901.jdbc4"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)


scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
