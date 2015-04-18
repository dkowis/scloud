package org.shlrm.scloud

import com.google.common.base.Charsets
import com.google.common.io.ByteSource
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.{BlobStoreContext, BlobStore}

trait BlobTestUtils extends BlobUtils {

  def putBlob(cloudPath: String, content: String)(implicit blobStore: BlobStore): Unit = {
    val (container, path) = parseCloudPath(cloudPath)

    val payload = ByteSource.wrap(content.getBytes(Charsets.UTF_8))
    val blob = blobStore.blobBuilder(path)
      .payload(payload)
      .contentLength(payload.size())
      .build()

    blobStore.putBlob(container, blob)
  }

  def prepInMemoryContainer():BlobStoreContext = {
    val context = ContextBuilder.newBuilder("transient").buildView(classOf[BlobStoreContext])

    implicit val blobStore = context.getBlobStore
    blobStore.createContainerInLocation(null, "testContainer")

    putBlob("testContainer://something.txt", "I made a file")
    putBlob("testContainer://another/something.txt", "This is a different one")
    putBlob("testContainer://another/oneMore/something.txt", "THREE OF THEM")
  }
}
