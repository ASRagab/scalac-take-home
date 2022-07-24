ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val zhttpVersion      = "2.0.0-RC10"
lazy val zioVersion        = "2.0.0"
lazy val circeVersion      = "0.14.1"
lazy val sttpClientVersion = "3.6.2"

addCommandAlias("precommit", ";clean;compile;scalafix;scalafmtAll;test")

lazy val root = (project in file("."))
  .settings(
    semanticdbEnabled         := true,
    semanticdbVersion         := scalafixSemanticdb.revision,
    name                      := "github-project",
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio"                           % zioVersion,
//      "dev.zio"                       %% "zio-interop-cats"              % "3.2.9.0",
      "io.circe"                      %% "circe-core"                    % circeVersion,
      "io.circe"                      %% "circe-generic"                 % circeVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "circe"                         % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "slf4j-backend"                 % sttpClientVersion,
      "io.d11"                        %% "zhttp"                         % zhttpVersion,
      "io.d11"                        %% "zhttp-test"                    % "2.0.0-RC9" % Test,
      "dev.zio"                       %% "zio-test"                      % zioVersion  % Test,
      "dev.zio"                       %% "zio-test-sbt"                  % zioVersion  % Test,
      "ch.qos.logback"                 % "logback-classic"               % "1.2.10"
    ),
    Compile / run / mainClass := Some("server.Main"),
    scalacOptions += "-Ywarn-unused:imports",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
