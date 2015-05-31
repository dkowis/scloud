package org.shlrm.scloud

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.options.ListContainerOptions

class Rm(cloudPath: String,
         recursive: Boolean = false,
         clobberContainer: Boolean = false)(implicit val blobStore: BlobStore) extends BlobUtils {
  val (container, blob) = parseCloudPath(cloudPath)

  def doit(): Unit = {
    import scala.collection.JavaConversions._
    if (clobberContainer) {
      blobStore.deleteContainer(container)
    } else if (recursive) {
      val listOptions = ListContainerOptions.Builder.inDirectory(blob).recursive()
      val blobs = blobStore.list(container, listOptions).map(_.getName).toList
      blobStore.removeBlobs(container, blobs)
    } else {
      blobStore.removeBlob(container, blob)
    }
  }

}
