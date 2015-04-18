package org.shlrm.scloud

import java.io.File
import java.nio.file.Files

import org.apache.commons.io.FileUtils
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStoreContext
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec, Matchers}

class ListSpec extends FunSpec
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with BlobTestUtils {

  //Set up the transient provider somehow
  var context: BlobStoreContext = _
  var tempDir: File = _

  before {
    context = prepInMemoryContainer()

    tempDir = {
      val t = Files.createTempDirectory("blobStore")

      t.toFile
    }
  }

  after {
    context.close()
    FileUtils.deleteDirectory(tempDir)
  }

  it("lists the contents of a container non-recursively") {
    implicit val blobStore = context.getBlobStore

    val l = new ListFileInfo("testContainer://")

    val fileInfoList = l.list()

    fileInfoList.size shouldBe 2
  }

  it("lists the contents of a container recursively") {
    implicit val blobStore = context.getBlobStore

    val l = new ListFileInfo("testContainer://", recursive = true)

    val fileInfoList = l.list()


    fileInfoList.size shouldBe 3
  }

  it("lists the details of a specific file") {
    implicit val blobStore = context.getBlobStore

    val l = new ListFileInfo("testContainer://something.txt")
    val fileInfoList = l.list()
    fileInfoList.size shouldBe 1
  }
}
