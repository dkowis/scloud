package org.shlrm.scloud

import java.io.File
import java.nio.file.Files

import com.google.common.base.Charsets
import com.google.common.io.ByteSource
import org.apache.commons.io.FileUtils
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStoreContext
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSpec, Matchers}

class GetSpec extends FunSpec
with Matchers
with BeforeAndAfter
with BeforeAndAfterAll
with BlobTestUtils {

  //Set up the transient provider somehow
  var context: BlobStoreContext = _
  var tempDir: File = _

  before {
    context = ContextBuilder.newBuilder("transient").buildView(classOf[BlobStoreContext])

    implicit val blobStore = context.getBlobStore
    blobStore.createContainerInLocation(null, "testContainer")

    putBlob("testContainer://something.txt", "I made a file")
    putBlob("testContainer://another/something.txt", "This is a different one")
    putBlob("testContainer://another/oneMore/something.txt", "THREE OF THEM")

    tempDir = {
      val t = Files.createTempDirectory("blobStore")

      t.toFile
    }
  }

  after {
    context.close()
    FileUtils.deleteDirectory(tempDir)
  }


  it("downloads an individual file to the current directory") {
    implicit val blobStore = context.getBlobStore
    val get = new Get("testContainer://something.txt", tempDir.getAbsolutePath)

    get.doit()
    //make sure there's a something.txt
    val outputFile = new File(tempDir, "something.txt")
    FileUtils.waitFor(outputFile, 10) shouldBe true
  }
  it("downloads an entire container recursively to the current directory") {
    implicit val blobStore = context.getBlobStore
    val get = new Get("testContainer://", tempDir.getAbsolutePath, recursive = true)

    get.doit()

    val outputFile1 = new File(tempDir, "something.txt")
    val outputFile2 = new File(tempDir, "another/something.txt")

    FileUtils.waitFor(outputFile1, 10) shouldBe true
    FileUtils.waitFor(outputFile2, 10) shouldBe true
  }
  it("downloads part of a container recursively to the current directory") {
    implicit val blobStore = context.getBlobStore
    val get = new Get("testContainer://another", tempDir.getAbsolutePath, recursive = true)

    get.doit()

    val outputFile1 = new File(tempDir, "something.txt")
    val outputFile2 = new File(tempDir, "oneMore/something.txt")

    FileUtils.waitFor(outputFile1, 10) shouldBe true
    FileUtils.waitFor(outputFile2, 10) shouldBe true

  }
  it("lists the files in a container") {
    pending
  }
}
