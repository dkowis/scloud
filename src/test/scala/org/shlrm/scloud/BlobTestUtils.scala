package org.shlrm.scloud

import java.io.File
import java.nio.file.{Files, Path}

import com.google.common.base.Charsets
import com.google.common.io.ByteSource
import org.apache.commons.io.FileUtils
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.{BlobStore, BlobStoreContext}

trait BlobTestUtils extends BlobUtils {

  def prepLocalFiles(): Path = {
    //Create a local directory full of files
    val root = Files.createTempDirectory("localTemp")

    FileUtils.write(new File(root.toFile, "file1.txt"), "This is some content")
    FileUtils.write(new File(root.toFile, "file2.txt"), "This is some content2")
    FileUtils.write(new File(root.toFile, "file3.txt"), "This is some content3")
    FileUtils.write(new File(root.toFile, "file4.txt"), "This is some content4")

    val subdir = new File(root.toFile, "subdir1")
    FileUtils.forceMkdir(subdir)

    FileUtils.write(new File(subdir, "file1.txt"), "This is some content")
    FileUtils.write(new File(subdir, "file2.txt"), "This is some content2")
    FileUtils.write(new File(subdir, "file3.txt"), "This is some content3")
    FileUtils.write(new File(subdir, "file4.txt"), "This is some content4")

    val subsubdir = new File(subdir, "deeper")
    FileUtils.forceMkdir(subsubdir)
    FileUtils.write(new File(subsubdir, "lol1.txt"), "This is some content")
    FileUtils.write(new File(subsubdir, "lol2.txt"), "This is some content2")
    FileUtils.write(new File(subsubdir, "lol3.txt"), "This is some content3")
    FileUtils.write(new File(subsubdir, "lol4.txt"), "This is some content4")

    val subdir2 = new File(root.toFile, "subdir2")
    FileUtils.forceMkdir(subdir2)

    FileUtils.write(new File(subdir2, "file1.txt"), "This is some content")
    FileUtils.write(new File(subdir2, "file2.txt"), "This is some content2")
    FileUtils.write(new File(subdir2, "file3.txt"), "This is some content3")
    FileUtils.write(new File(subdir2, "file4.txt"), "This is some content4")

    root
  }


  def putBlob(cloudPath: String, content: String)(implicit blobStore: BlobStore): Unit = {
    val (container, path) = parseCloudPath(cloudPath)

    val payload = ByteSource.wrap(content.getBytes(Charsets.UTF_8))
    val blob = blobStore.blobBuilder(path)
      .payload(payload)
      .contentLength(payload.size())
      .build()

    blobStore.putBlob(container, blob)
  }

  def prepInMemoryContainer(): BlobStoreContext = {
    val context = ContextBuilder.newBuilder("transient").buildView(classOf[BlobStoreContext])

    implicit val blobStore = context.getBlobStore
    blobStore.createContainerInLocation(null, "testContainer")

    putBlob("testContainer://something.txt", "I made a file")
    putBlob("testContainer://another/something.txt", "This is a different one")
    putBlob("testContainer://another/oneMore/something.txt", "THREE OF THEM")
    context
  }
}
