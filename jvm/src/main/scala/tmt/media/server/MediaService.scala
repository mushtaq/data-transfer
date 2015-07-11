package tmt.media.server

import java.io.File
import java.nio.file.Files

import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.io.{SynchronousFileSink, SynchronousFileSource}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import tmt.common._
import tmt.library.SourceExtensions.RichSource

import scala.concurrent.Future

class MediaService(actorConfigs: ActorConfigs, settings: AppSettings) {

  import actorConfigs._

  val fileIoDispatcher = system.dispatchers.lookup("akka.stream.default-file-io-dispatcher")

  def files = Source(() => Producer.files(settings.framesInputDir))

  val parallelism = 1

  def sendBytes = files.mapAsync(parallelism)(readFile).map(ByteString.apply)
  def sendImages = files.mapAsync(parallelism)(readImage)
  def sendMessages = files.map(SynchronousFileSource(_)).map(BinaryMessage.apply)

  def copyBytes(byteArrays: Source[ByteString, Any]) = byteArrays.zip(fileNamesToWrite)
    .mapAsync(parallelism) { case (data, file) => copyFile(file, data.toArray) }
    .runWith(Sink.ignore)

  def copyMovie(name: String, byteArrays: Source[ByteString, Any]) =  {
    val file = new File(s"${settings.moviesOutputDir}/$name")
    println(s"writing to $file")
    byteArrays.runWith(SynchronousFileSink(file)).map(_ => ())
  }

  def copyImages(images: Source[Image, Any]) = images.mapAsync(parallelism)(copyImage).runWith(Sink.ignore)

  def listMovies = Source(() => new File(settings.moviesInputDir).list().iterator)

  private def readFile(file: File) = Future(Files.readAllBytes(file.toPath))(fileIoDispatcher)
  private def readImage(file: File) = readFile(file).map(data => Image(file.getName, data))(fileIoDispatcher)

  private def fileNamesToWrite = Source(() => Producer.numbers()).map(index => f"out-image-$index%05d.jpg")

  private def copyFile(name: String, data: Array[Byte]) = Future {
    val file = new File(s"${settings.framesOutputDir}/$name")
    println(s"writing to $file")
    Files.write(file.toPath, data)
  }(fileIoDispatcher)

  private def copyImage(image: Image) = copyFile(image.name, image.bytes)
}