ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val zhttpVersion      = "2.0.0-RC2"
lazy val zioVersion        = "2.0.0-RC1"
lazy val circeVersion      = "0.14.1"
lazy val sttpClientVersion = "3.4.1"

lazy val root = (project in file("."))
  .settings(
    name := "github-project",
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio"                           % zioVersion,
      "dev.zio"                       %% "zio-interop-cats"              % "3.2.9.0",
      "io.circe"                      %% "circe-core"                    % circeVersion,
      "io.circe"                      %% "circe-generic"                 % circeVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "circe"                         % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "slf4j-backend"                 % sttpClientVersion,
      "io.d11"                        %% "zhttp"                         % zhttpVersion,
      "io.d11"                        %% "zhttp-test"                    % zhttpVersion % Test,
      "dev.zio"                       %% "zio-test"                      % zioVersion   % Test,
      "ch.qos.logback"                 % "logback-classic"               % "1.2.10"
    ),
    Compile / run / mainClass := Some("server.Main"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
