package org.shlrm.scloud

import java.io.{InputStream, PrintStream}
import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStore
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext

import scala.util.{Failure, Success, Try}

class Scloud {

  val provider = "rackspace-cloudfiles-us"

  def execute(args: Array[String], in: InputStream, out: PrintStream, err: PrintStream): Int = {

    val conf = new Conf(args)

    //Match on the nested list of things to get done
    //Apparently I cannot match on the nested list :(
    conf.subcommands match {
      case List(conf.files, conf.files.sync) => {
        //Create a sync object, set up the cloud junk, make it go
        loadAuthConfig(conf) match {
          case Success(x) => {
            implicit val blobStore = x

            val syncConf = conf.files.sync
            val sync = new CloudSync(Paths.get(syncConf.localPath()), syncConf.remotePath(), syncConf.purge())
            sync.doit()
            0
          }
          case Failure(x) => {
            println("Unable to parse config, or no config specified!")
            println("\t" + x.getMessage)
            1
          }
        }
      }
      case List(conf.files) => {
        println("INVALID FILES USAGE")
        conf.files.printHelp()
        1
      }
      case _ =>
        println("INVALID USAGE")
        conf.printHelp()
        1
    }
  }

  def loadAuthConfig(conf: Conf): Try[BlobStore] = {
    //Create a BlobStore from however we figure out the configuration options
    try {
      val credsConfig = ConfigFactory.parseFile(conf.credsFile())

      //Ensure that we have a username, an API key, and a region?
      credsConfig.checkValid(ConfigFactory.load("org/shlrm/scloud/referenceConfig.conf"), "scloud")

      val scloudConfig = credsConfig.getConfig("scloud")

      val username = scloudConfig.getString("username")
      val apikey = scloudConfig.getString("apikey")
      val region = scloudConfig.getString("region")

      val context = ContextBuilder.newBuilder(provider)
        .credentials(username, apikey)
        .buildView(classOf[RegionScopedBlobStoreContext])

      //Construct a blobstore for the specified region!
      Success(context.getBlobStore(region))
    } catch {
      case e: Exception => {
        Failure(e)
      }
    }
  }
}
