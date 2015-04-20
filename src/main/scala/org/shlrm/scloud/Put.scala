package org.shlrm.scloud

import java.nio.file.{Paths, Path}

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.{Blob, StorageType}

class Put(localPath: Path,
          cloudPath: String,
          recursive: Boolean = false)
         (implicit val blobStore: BlobStore) extends BlobUtils {

  val (containerName, filePath) = parseCloudPath(cloudPath)

  def uploadFile(): Unit = {
    //TODO: probably deal with remote paths and such....
    val localFileName = localPath.getFileName.toString

    val blob: Blob = if (filePath.isEmpty || filePath.equals("/")) {
      blobStore.blobBuilder(localFileName)
        .payload(localPath.toFile)
        .build()
    } else {
      val checking = Option(blobStore.getBlob(containerName, filePath))

      checking.map { existing =>
        //This means it exists, so handle two of the logic paths
        if (existing.getMetadata.getType == StorageType.RELATIVE_PATH) {
          blobStore.blobBuilder(filePath + "/" + localFileName)
            .payload(localPath.toFile)
            .build()
        } else {
          ???
        }
      } getOrElse {
        //TODO: how to know if they're specifying a new file or not...
        //TODO: have to split the file path up into parts to create the subcontainers first
        filePath.split("/").foldLeft("") { (path, current) =>
          blobStore.createDirectory(containerName, path + "/" + current)
          println(s"creating Directory: $path/$current")
          path + "/" + current
        }

        blobStore.blobBuilder(filePath)
          .payload(localPath.toFile)
          .build()
      }

      //TODO: check to see if the filepath is a container, put the file in there
      // if the file path is not specified, it goes in the root of the container
      // if there is a file path, and it's not a container, then it's a new file name
    }


    blobStore.putBlob(containerName, blob)
  }

}
