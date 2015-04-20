package org.shlrm.scloud

import java.io.File
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import org.jclouds.blobstore.domain.StorageType
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class PutSpec extends FunSpec with Matchers with BlobTestUtils with BeforeAndAfter {

  val context = prepInMemoryContainer()

  def writeTempFile(path: Path, content: String): Unit = {
    FileUtils.writeStringToFile(path.toFile, content)
  }

  var tempDir:File = _

  before {
    tempDir = {
      val t = Files.createTempDirectory("blobPutSpec")

      //put some files in it
      writeTempFile(Paths.get(t.toString, "someFile.txt"), "This is some file")
      writeTempFile(Paths.get(t.toString, "someFile2.txt"), "This is some other file")
      writeTempFile(Paths.get(t.toString, "subdir", "someFile.txt"), "This is some file in a subdir")
      writeTempFile(Paths.get(t.toString, "subdir", "someFile2.txt"), "This is some other file in a subdir")
      writeTempFile(Paths.get(t.toString, "subdir", "subsubdir", "dirception.txt"), "we have to go deeper")

      t.toFile
    }
  }

  after {
    FileUtils.deleteDirectory(tempDir)
  }

  it("can upload an individual file into the root of a container") {
    implicit val blobStore = context.getBlobStore

    val put = new Put(Paths.get(tempDir.toString, "someFile.txt"), "testContainer://")

    put.uploadFile()

    //Assert that the testContainer has the file
    blobStore.blobExists("testContainer", "someFile.txt") shouldBe true
  }
  it("can upload an individual file into a subdirectory of a container, creating the subdir") {
    implicit val blobStore = context.getBlobStore

    val put = new Put(Paths.get(tempDir.toString, "someFile.txt"), "testContainer://woo")

    put.uploadFile()

    //blobStore.directoryExists("testContainer", "another") shouldBe true

//    blobStore.directoryExists("testContainer", "woo") shouldBe true
    //blobStore.blobMetadata("testContainer", "woo/").getType shouldBe StorageType.RELATIVE_PATH
    //Assert that the testContainer has the file in woo
    blobStore.blobExists("testContainer", "woo/someFile.txt") shouldBe true
  }
  it("can upload an individual file to a specific different file") {
    pending
  }
  it("can upload an individual file overwriting") {
    pending
  }
  it("can upload a directory of files") {
    pending
  }
  it("can upload a directory of files overwriting all") {
    pending
  }
  it("can upload a directory of files recursively") {
    pending
  }
  it("can upload a directory of files recursively overwriting all") {
    pending
  }
}
