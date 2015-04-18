package org.shlrm.scloud

import java.io.File

import org.rogach.scallop.{Subcommand, ScallopConf}

/**
 * scloud
 * files
 * get [-r|--recursive] [-o|--output dir] cloudPath
 * put [-r|--recursive] [--overwrite] [--delete] sourcePath cloudPath
 * delete [-r|--recursive] cloudPath
 */
object Main extends App {
  println("Woot")

  println(s"ARGS: ${args.mkString(" ")}")

  object Conf extends ScallopConf(args) {
    val username = opt[String](descr = "username")
    val apikey = opt[String](descr = "api key")
    val credsFile = opt[File](descr = "path to credentials file")
    val files = new Subcommand("files") {
      val list = new Subcommand("list") {
        val cloudPath = trailArg[String](descr = "path in the cloud for files")
      }
      val get = new Subcommand("get") {
        val recursive = opt[Boolean](descr = "Recursively get files", default = Some(false))
        val output = opt[String](descr = "Output directory", default = Some("."))
        val cloudPath = trailArg[String](descr = "Path in the cloud for files container/something/something_else")
      }
      val put = new Subcommand("put") {
        val recursive = opt[Boolean](descr = "Recursively put files")
        val overwrite = opt[Boolean](descr = "overwrite existing files", noshort = true)
        val delete = opt[Boolean](descr = "delete files on remote not on local", noshort = true)
        val cloudPath = trailArg[String](descr = "Path in the cloud for files container/something/something_else")
      }
      val delete = new Subcommand("delete") {
        val recursive = opt[Boolean](descr = "Recursively delete files")
        val cloudPath = trailArg[String](descr = "Path in the cloud for files container/something/something_else")
      }
    }
    version("scloud 1.0 (c) 2015 David Kowis")
    //footer("Footer!")
  }



  println(Conf.summary)
}
