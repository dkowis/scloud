package org.shlrm.scloud

trait BlobUtils {

  def parseCloudPath(cloudPath: String): (String, String) = {
    val splits = cloudPath.split("://")
    if (splits.size == 1) {
      (splits(0), "")
    } else {
      (splits(0), splits(1))
    }
  }
}
