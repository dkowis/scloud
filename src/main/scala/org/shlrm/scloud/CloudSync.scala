package org.shlrm.scloud

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.options.ListContainerOptions
import org.jclouds.domain.{LocationBuilder, Location}

import scala.concurrent.duration.Duration

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

    import scala.concurrent._
    import ExecutionContext.Implicits.global


    //I'll have a list of files now
    val totalFiles = foundFiles.size
    val futures: List[Future[String]] = foundFiles.toList.zipWithIndex.map {
      case (local, index) => {
        //Throw the stuff into a future, and then execute it eventually, awaiting the output
        Future {
          val relativePath = localPath.relativize(local)
          val contentType = Files.probeContentType(local)
          println(s"$index of $totalFiles Synchronizing $local to $cloudContainer://$relativePath")
          val blob = blobStore.blobBuilder(base + relativePath.toString)
            .payload(local.toFile)
            .contentType(contentType)
            .build()

          blobStore.putBlob(cloudContainer, blob)
        }
      }
    }

    //Await all the futures in a nice list
    val completedList = Await.result(Future.sequence(futures), Duration.Inf)
    println(s"%100 COMPLETE uploaded ${completedList.size} files!")

    if (purge) {
      println("Getting list of files in container, to prepare for purging....")
      //Clean out files in the remote location that aren't part of the list
      import scala.collection.JavaConversions._
      val listedItems = blobStore.list(cloudContainer, ListContainerOptions.Builder.recursive()).toSet
      //This didn't work at all! I deleted everything!

      //Order matters, we want to find things that are in the cloud but not local
      val differences = listedItems.map(_.getName) &~ foundFiles.map(localPath.relativize(_).toString)

      differences.map { name =>
        println(s"Purging $cloudContainer://$name")
        blobStore.removeBlob(cloudContainer, name)
      }
    }

    println("ALL DONE")
  }

}
