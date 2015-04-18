package org.shlrm.scloud

import java.nio.file.{Files, Paths}

import org.jclouds.blobstore.BlobStore


class Get(cloudPath: String,
          outputDir: String = ".",
          recursive: Boolean = false)(implicit val blobStore: BlobStore) extends BlobUtils {

  //Download a file, or recursively files into this directory

  val (containerName, filePath) = parseCloudPath(cloudPath)


  def doit(): Unit = {
    if(recursive) {
      import org.jclouds.blobstore.options.ListContainerOptions.Builder._

      val blobList = blobStore.list(containerName, inDirectory(filePath).recursive())

      import scala.collection.JavaConversions._

      blobList.size()

      blobList.foreach { meta =>
        val name = meta.getName
        val blob = blobStore.getBlob(containerName, name)

        val outputPath = Paths.get(outputDir, name)
        println(s"Working on $outputPath")
        //TODO: need to output relative sauce
        val components = outputPath.resolve(Paths.get(outputDir, filePath))
        println(s"Resolved to: ${outputPath.subpath(0, components.getNameCount)}")
        Files.createDirectories(outputPath.getParent)
        Files.copy(blob.getPayload.openStream(), outputPath)
      }
    } else {
      val blob = blobStore.getBlob(containerName, filePath)
      val blobName = blob.getMetadata.getName

      //val outputFile = new File(outputDir, blobName)
      val outputPath = Paths.get(outputDir, blobName)
      Files.copy(blob.getPayload.openStream(), outputPath)
    }
  }

}
