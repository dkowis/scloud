package org.shlrm.scloud

import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.options.ListContainerOptions
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class RmSpec extends FunSpec with Matchers with BlobTestUtils with BeforeAndAfter {

  var context: BlobStoreContext = _

  before {
    context = prepInMemoryContainer()
  }


  it("can delete a file from a container") {
    implicit val blobStore = context.getBlobStore
    val rm = new Rm("testContainer://something.txt")
    rm.doit()

    blobStore.blobExists("testContainer", "something.txt") shouldBe false
  }
  it("can empty an entire container") {
    implicit val blobStore = context.getBlobStore
    val rm = new Rm("testContainer://", recursive = true)
    rm.doit()

    blobStore.countBlobs("testContainer", ListContainerOptions.Builder.recursive()) shouldBe 0
  }
  it("can empty an entire container's subpath") {
    implicit val blobStore = context.getBlobStore
    val rm = new Rm("testContainer://subdir1", recursive = true)
    rm.doit()

    blobStore.countBlobs("testContainer", ListContainerOptions.Builder.inDirectory("subdir1").recursive()) shouldBe 0
    blobStore.countBlobs("testContainer", ListContainerOptions.Builder.recursive()) should be > 0l // TYPES!
  }
  it("can delete a full container") {
    implicit val blobStore = context.getBlobStore
    val rm = new Rm("testContainer://", clobberContainer = true)
    rm.doit()

    blobStore.containerExists("testContainer") shouldBe false
  }
  it("can delete an empty container") {
    implicit val blobStore = context.getBlobStore
    blobStore.createContainerInLocation(null, "emptyContainer")
    blobStore.containerExists("emptyContainer") shouldBe true
    blobStore.list("emptyContainer", ListContainerOptions.Builder.recursive()) shouldBe empty

    val rm = new Rm("emptyContainer://", clobberContainer = true)
    rm.doit()
    blobStore.containerExists("emptyContainer") shouldBe false
  }
}
