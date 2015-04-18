package org.shlrm.scloud

import com.google.common.base.Charsets
import com.google.common.io.ByteSource
import org.jclouds.blobstore.BlobStore

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
}
