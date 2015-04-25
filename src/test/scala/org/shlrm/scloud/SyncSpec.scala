package org.shlrm.scloud

import java.nio.file.Path

import com.google.common.hash.Hashing
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.{BlobStore, BlobStoreContext}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class SyncSpec extends FunSpec with Matchers with BlobTestUtils with BeforeAndAfter {

  var filesRoot: Path = _
  val context = ContextBuilder.newBuilder("transient").buildView(classOf[BlobStoreContext])

  before {
    filesRoot = prepLocalFiles()
  }

  after {
    FileUtils.deleteDirectory(filesRoot.toFile)
  }

  describe("synchronizing a local directory recursively with a location in the cloud") {
    it("ensures all local files are uploaded to an empty cloud container at root") {
      implicit val blobStore = context.getBlobStore
      blobStore.createContainerInLocation(null, "emptySyncContainer")

      val cloudSync = new CloudSync(filesRoot, "emptySyncContainer://")

      cloudSync.doit()

      blobStore.blobExists("emptySyncContainer", "file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "file4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subdir1/file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/file4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subdir1/deeper/lol1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/deeper/lol2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/deeper/lol3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir1/deeper/lol4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subdir2/file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir2/file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir2/file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subdir2/file4.txt") shouldBe true

    }
    it("ensures all local files are uploaded to an empty cloud container at a relative path") {
      implicit val blobStore = context.getBlobStore
      blobStore.createContainerInLocation(null, "emptySyncContainer")

      val cloudSync = new CloudSync(filesRoot, "emptySyncContainer://subroot")

      cloudSync.doit()

      blobStore.blobExists("emptySyncContainer", "subroot/file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/file4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/file4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/deeper/lol1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/deeper/lol2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/deeper/lol3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir1/deeper/lol4.txt") shouldBe true

      blobStore.blobExists("emptySyncContainer", "subroot/subdir2/file1.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir2/file2.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir2/file3.txt") shouldBe true
      blobStore.blobExists("emptySyncContainer", "subroot/subdir2/file4.txt") shouldBe true


    }

    def compareMd5(local: Path, remote: String)(implicit blobStore: BlobStore) = {
      val (container, path) = parseCloudPath(remote)

      val code = Files.hash(local.toFile, Hashing.md5())
      val remoteCode = blobStore.blobMetadata(container, path).getContentMetadata.getContentMD5AsHashCode

      assert(code == remoteCode, s"MD5s didn't match for ${local}:${remote}")
    }

    it("uploads all local files overwriting existing files") {
      //Prep a cloud container containing slightly different items
      implicit val blobStore = context.getBlobStore
      blobStore.createContainerInLocation(null, "syncContainer")

      putBlob("syncContainer://file1.txt", "This is a different pile of content")
      putBlob("syncContainer://file2.txt", "This is a differing pile of content")
      putBlob("syncContainer://file3.txt", "This is a pile of content that is different")
      putBlob("syncContainer://file4.txt", "This is some content that is not the same")

      putBlob("syncContainer://subdir1/deeper/lol1.txt", "This is a lolcontent")

      val cloudSync = new CloudSync(filesRoot, "syncContainer://")
      cloudSync.doit()

      //Ensure that the md5sum of the new files matches the md5sum of the local files, ensuring they're overwritten
      //Can use the guava stuff
      compareMd5(filesRoot.resolve("file1.txt"), "syncContainer://file1.txt")
      compareMd5(filesRoot.resolve("file2.txt"), "syncContainer://file2.txt")
      compareMd5(filesRoot.resolve("file3.txt"), "syncContainer://file3.txt")
      compareMd5(filesRoot.resolve("file4.txt"), "syncContainer://file4.txt")
      compareMd5(filesRoot.resolve("subdir1/deeper/lol1.txt"), "syncContainer://subdir1/deeper/lol1.txt")

    }
    it("uploads all local files overwriting existing files and deleting files not in local") {
      //Prep a cloud container containing slightly different items
      implicit val blobStore = context.getBlobStore
      blobStore.createContainerInLocation(null, "syncContainer")

      putBlob("syncContainer://file1.txt", "This is a different pile of content")
      putBlob("syncContainer://file2.txt", "This is a differing pile of content")
      putBlob("syncContainer://file3.txt", "This is a pile of content that is different")
      putBlob("syncContainer://file4.txt", "This is some content that is not the same")

      putBlob("syncContainer://subdir1/deeper/lol1.txt", "This is a lolcontent")

      putBlob("syncContainer://extra/things.txt", "lol this should be deleted")
      putBlob("syncContainer://extra/thing1.txt", "lol this should be deleted")
      putBlob("syncContainer://extra/thing2.txt", "lol this should be deleted")
      putBlob("syncContainer://extra/deep/location/of/stuff/lol.txt", "lol this should be deleted")

      val cloudSync = new CloudSync(filesRoot, "syncContainer://", purge = true)
      cloudSync.doit()

      blobStore.blobExists("syncContainer", "extra/things.txt") shouldNot be(true)
      blobStore.blobExists("syncContainer", "extra/thing1.txt") shouldNot be(true)
      blobStore.blobExists("syncContainer", "extra/thing2.txt") shouldNot be(true)
      blobStore.blobExists("syncContainer", "extra/deep/location/of/stuff/lol.txt") shouldNot be(true)
    }
  }
}
