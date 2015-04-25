lazy val root = (project in file(".")).
  settings(
    name := "scloud",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.2.1",
      //"org.apache.jclouds" % "jclouds-all" % "1.9.0",
      "org.apache.jclouds.driver" % "jclouds-slf4j" % "1.9.0",
      "org.apache.jclouds.labs" % "rackspace-cloudfiles-us" % "1.9.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.2",
      "org.apache.logging.log4j" % "log4j-api" % "2.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "org.rogach" %% "scallop" % "0.9.5",
      "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "commons-io" % "commons-io" % "2.4" % "test"
    ),
    mainClass := Some("org.shlrm.scloud.Main"),
    assemblyMergeStrategy in assembly := {
      case PathList(ps @_*) if ps.last == "pom.properties" || ps.last == "pom.xml" => MergeStrategy.deduplicate
      case PathList("META-INF", "services", xs @ _*) => MergeStrategy.filterDistinctLines
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

