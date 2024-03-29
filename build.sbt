val AkkaVersion = "2.5.25"
val LogbackVersion = "1.2.3"
scalaVersion := "2.12.9"

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val sharedSettings = Seq(
  organization := "oen",
  scalaVersion := "2.12.9",
  version := "0.1.0-SNAPSHOT",
  turbo := true,
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.7.0",
    "org.typelevel" %% "cats-core" % "1.6.1",
    "io.circe" %%% "circe-generic" % "0.11.1",
    "io.circe" %%% "circe-literal" % "0.11.1",
    "io.circe" %%% "circe-generic-extras" % "0.11.1",
    "io.circe" %%% "circe-parser" % "0.11.1",
    "io.scalaland" %%% "chimney" % "0.3.2",
    "com.softwaremill.quicklens" %%% "quicklens" % "1.4.12"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypartial-unification",
    "-language:higherKinds"
  )
)

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.7",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2",
    "com.payalabs" %%% "scalajs-react-bridge" % "0.8.1",
    "io.suzaku" %%% "diode" % "1.1.5",
    "io.suzaku" %%% "diode-react" % "1.1.5.142"
  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "16.7.0",
    "react-dom" -> "16.7.0",
    "react-popper" -> "1.3.4",
    "bootstrap" -> "4.3.1",
    "jquery" -> "3.4.1",
    "react-canvas-draw" -> "1.0.2",
    "react-color" -> "2.17.3"
  ),
  webpackBundlingMode := BundlingMode.LibraryAndApplication(), // LibraryOnly() for faster dev builds
  scalaJSUseMainModuleInitializer := true,
  localUrl := ("0.0.0.0", 12345)
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.4.0",
    "com.typesafe.akka" %% "akka-http"   % "10.1.9",
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.27.0",
    "ch.megard" %% "akka-http-cors" % "0.4.1",
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  ),
  target := baseDirectory.value / ".." / "target"
)

lazy val d00dle =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full).in(file("."))
    .settings(sharedSettings)
    .jsSettings(jsSettings)
    .jvmSettings(jvmSettings)

lazy val d00dleJS = d00dle.js
  .enablePlugins(WorkbenchPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)

lazy val d00dleJVM = d00dle.jvm
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "oracle/graalvm-ce:19.2.0",
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in(d00dleJS, Compile)).value,
    mappings.in(Universal) ++= webpack.in(Compile, fullOptJS).in(d00dleJS, Compile).value.map { f =>
      f.data -> s"assets/${f.data.getName()}"
    },
    mappings.in(Universal) ++= Seq(
      (target in(d00dleJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "bootstrap" / "dist" / "css" / "bootstrap.min.css" -> "assets/bootstrap.min.css"
    ),
    bashScriptExtraDefines += """addJava "-Dassets=${app_home}/../assets""""
  )

disablePlugins(RevolverPlugin)
