package org.shlrm.scloud

/**
 * scloud
 * files
 * # I want to public stuff to cloud files for my blogoblag
 * sync [--purge] <localDir> <remoteDir>
 */
object Main extends App {
  val scloud = new Scloud()

  val code = scloud.execute(args, System.in, System.out, System.err)

  sys.exit(code)
}
