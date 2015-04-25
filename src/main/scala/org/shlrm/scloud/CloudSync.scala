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

    //Get a quick list of all the files to do x of something?

    //Recursively put all the files!
    //also collect a list of the files we care about, mutable state!
    var foundFiles = Set.empty[Path]
    class AcquireFiles extends SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        foundFiles = foundFiles + file
        FileVisitResult.CONTINUE
      }

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        FileVisitResult.CONTINUE
      }
    }

    val processor = new AcquireFiles()

    Files.walkFileTree(localPath, processor)

    //I'll have a list of files now
    val totalFiles = foundFiles.size
    foundFiles.toList.zipWithIndex.foreach {
      case (local, index) => {
        val relativePath = localPath.relativize(local)
        val percentCompleted = (index / totalFiles.toFloat * 100).toInt
        println(s"%$percentCompleted: Synchronizing $local to $cloudContainer://$relativePath")
        val blob = blobStore.blobBuilder(base + relativePath.toString)
          .payload(local.toFile)
          .build()

        blobStore.putBlob(cloudContainer, blob)
      }
    }

    if (purge) {
      //Clean out files in the remote location that aren't part of the list
      import scala.collection.JavaConversions._
      val listedItems = blobStore.list(cloudContainer, ListContainerOptions.Builder.recursive()).toSet

      //Order matters, we want to find things that are in the cloud but not local
      val differences = listedItems.map(_.getName) &~ foundFiles.map(_.toString)

      differences.foreach { name =>
        println(s"Purging $cloudContainer://$name")
        blobStore.removeBlob(cloudContainer, name)
      }
    }

  }

}
