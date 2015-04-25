package org.shlrm.scloud

import java.io.{File, InputStream, PrintStream}

import org.rogach.scallop.{Subcommand, ScallopConf}

class Scloud {

  def execute(args: Array[String], in: InputStream, out: PrintStream, err: PrintStream): Int = {
    println(s"ARGS: ${args.mkString(" ")}")

    object Conf extends ScallopConf(args) {
      val username = opt[String](descr = "username")
      val apikey = opt[String](descr = "api key")
      val credsFile = opt[File](descr = "path to credentials file")
      val files = new Subcommand("files") {
        val sync = new Subcommand("sync") {
          val purge = opt[Boolean](descr = "Purge files in the remote location that aren't in local", default = Some(false), noshort = true)
          val localPath = trailArg[String](descr = "local path")
          val remotePath = trailArg[String](descr = "remote path")
        }
      }
      version("scloud 1.0 (c) 2015 David Kowis")
      //footer("Footer!")
    }

    println(Conf.summary)
    0
  }
}
