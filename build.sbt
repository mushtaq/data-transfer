name := "tmt-root"

lazy val root = project.in(file(".")).
  aggregate(dtJs, dtJvm)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val dataTransfer = crossProject.in(file("."))
  .settings(
    organization := "tmt",
    name := "data-transfer",
    scalaVersion := "2.11.7",
    version := "0.1-SNAPSHOT",
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    updateOptions := updateOptions.value.withCachedResolution(true),
    libraryDependencies += "me.chrons" %%% "boopickle" % "1.0.0",
    libraryDependencies += "com.softwaremill.macwire" %% "macros" % "1.0.5"
  )
  .jvmSettings(Revolver.settings: _*)
  .jvmSettings(
    fork := true,
    libraryDependencies ++= Dependencies.jvmLibs,
    mainClass in Revolver.reStart := Some("tmt.media.server.MediaServer")
  )
  .jsSettings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    scalaJSStage in Global := FastOptStage,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "org.monifu" %%% "monifu" % "1.0-M2"
    )
  )

lazy val dtJvm = dataTransfer.jvm.settings(
  (resourceGenerators in Compile) <+=
    (fastOptJS in Compile in dtJs, packageScalaJSLauncher in Compile in dtJs).map((f1, f2) => Seq(f1.data, f2.data)),
  watchSources <++= (watchSources in dtJs)
)

lazy val dtJs = dataTransfer.js
