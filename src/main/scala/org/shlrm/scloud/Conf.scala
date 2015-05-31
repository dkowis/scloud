package org.shlrm.scloud

import java.io.File

import org.rogach.scallop.{Subcommand, ScallopConf}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val credsFile = opt[File](descr = "path to credentials file")
  val files = new Subcommand("files") {
    val sync = new Subcommand("sync") {
      val purge = opt[Boolean](descr = "Purge files in the remote location that aren't in local", default = Some(false), noshort = true)
      val localPath = trailArg[String](descr = "local path")
      val remotePath = trailArg[String](descr = "remote path")
    }
    val rm = new Subcommand("rm") {
      val recursive = opt[Boolean](descr = "Recursively empty container", default = Some(false))
      val deleteContainer = opt[Boolean](descr = "Delete the entire container! (all blobs will be gone)", noshort = true, default = Some(false))
      val remotePath = trailArg[String](descr = "remote path")
    }
  }
  version("scloud 1.0 (c) 2015 David Kowis")
  //footer("Footer!")

}
