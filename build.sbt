name := "reacting"

enablePlugins(ScalaJSPlugin)

scalaVersion := "2.11.7"
scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-unchecked",
  "-Xlint"
)

libraryDependencies ++= Seq(
  "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
  "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.4"
)
jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % "0.14.7" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
  "org.webjars.bower" % "react" % "0.14.7" / "react-dom.js" minified  "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
  "org.webjars" % "marked" % "0.3.2-1" / "marked.js"
)

scalaJSUseRhino := false
skip in packageJSDependencies := false
persistLauncher in Compile := true
persistLauncher in Test := false
