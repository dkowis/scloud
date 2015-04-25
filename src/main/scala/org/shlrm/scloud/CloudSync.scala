package org.shlrm.scloud

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.options.ListContainerOptions
import org.jclouds.domain.{LocationBuilder, Location}

class CloudSync(localPath: Path, cloudPath: String, purge: Boolean = false)(implicit val blobStore: BlobStore) extends BlobUtils {

  val (cloudContainer, remotePath) = parseCloudPath(cloudPath)

  def doit() = {

    val base = if (remotePath == "") {
      ""
    } else {
      remotePath + "/"
    }

    //Recursively put all the files!
    //also collect a list of the files we care about, mutable state!
    var foundFiles = Set.empty[Path]
    class UploadFile extends SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val relativePath = localPath.relativize(file)
        println(s"Synchronizing $file to $cloudContainer://$relativePath")
        foundFiles = foundFiles + relativePath
        val blob = blobStore.blobBuilder(base + relativePath.toString)
          .payload(file.toFile)
          .build()

        blobStore.putBlob(cloudContainer, blob)
        FileVisitResult.CONTINUE
      }

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        FileVisitResult.CONTINUE
      }
    }

    val processor = new UploadFile()

    Files.walkFileTree(localPath, processor)

    if (purge) {
      //Clean out files in the remote location that aren't part of the list
      import scala.collection.JavaConversions._
      val listedItems = blobStore.list(cloudContainer, ListContainerOptions.Builder.recursive()).toSet

      //Order matters, we want to find things that are in the cloud but not local
      val differences = listedItems.map(_.getName) &~ foundFiles.map(_.toString)

      differences.foreach { name =>
        blobStore.removeBlob(cloudContainer, name)
      }
    }

  }

}
