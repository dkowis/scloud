package org.shlrm.scloud

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.StorageType

case class FileInfo(name: String, size: Option[Long], storageType: StorageType)

class ListFileInfo(cloudPath: String,
                   recursive: Boolean = false)
                  (implicit val blobStore: BlobStore)
  extends BlobUtils {

  val (container, path) = parseCloudPath(cloudPath)

  def list(): Set[FileInfo] = {
    import org.jclouds.blobstore.options.ListContainerOptions.Builder._

    import scala.collection.JavaConversions._
    val bloblist = if (this.recursive) {
      //A recursive list will only list the files, not their individual directories...
      blobStore.list(container, inDirectory(path).recursive()).toSet
    } else if (blobStore.blobExists(container, path)) {
      //It's a file!
      Set(blobStore.blobMetadata(container, path))
    } else {
      blobStore.list(container, inDirectory(path)).toSet
    }

    bloblist.map { meta =>
      if (meta.getType.equals(StorageType.BLOB)) {
        new FileInfo(meta.getName, Some(meta.getSize), meta.getType)
      } else {
        new FileInfo(meta.getName, None, meta.getType)
      }
    }.toSet
  }
}
