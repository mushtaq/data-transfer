package tmt.performace.utils

import java.io.File

import tmt.common.{AppSettings, Producer}

//we can pass an instance of filter method
class FilteredImageProducer(appSettings: AppSettings, minFileSize: Int, maxFileSize: Int) extends Producer(appSettings) {
  override def files(dir: String): Iterator[File] =
    super.files(dir)
    .filter(file => file.length() >= minFileSize && file.length() < maxFileSize)
}
