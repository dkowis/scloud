lazy val root = (project in file(".")).
  settings(
    name := "scloud",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      "org.apache.jclouds" % "jclouds-all" % "1.9.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.2",
      "org.apache.logging.log4j" % "log4j-api" % "2.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "org.rogach" %% "scallop" % "0.9.5",
      "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "commons-io" % "commons-io" % "2.4" % "test"
    ),
    mainClass := Some("org.shlrm.scloud.Main")
  )

