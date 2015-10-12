package tmt.transformations

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.concurrent.Executors
import javax.imageio.ImageIO

import akka.http.scaladsl.model.DateTime
import akka.stream.scaladsl.Source
import org.imgscalr.AsyncScalr
import org.imgscalr.Scalr.Rotation
import tmt.actors.SubscriptionService
import tmt.app.ActorConfigs
import tmt.io.ImageWriteService
import tmt.shared.models.{ImageMetric, Image}
import scala.async.Async._
import scala.concurrent.ExecutionContext

class ImageTransformations(
  imageWriteService: ImageWriteService,
  actorConfigs: ActorConfigs,
  imageSubscriber: SubscriptionService[Image]) {

  import actorConfigs._

  lazy val images: Source[Image, Unit] = imageSubscriber.source

  lazy val filteredImages = images.filter(_.name.contains("9"))

  lazy val copiedImages = images.mapAsync(1) { image =>
    imageWriteService.copyImage(image).map(_ => image)
  }

  lazy val imageMetrics = images.map(image => ImageMetric(image.name, image.size, DateTime.now.clicks))

  lazy val rotatedImages = images.mapAsync(8) { originalImage =>
    async {
      val bufferedImage = ImageIO.read(new ByteArrayInputStream(originalImage.bytes))
      val rotatedBufferedImage = AsyncScalr.rotate(bufferedImage, Rotation.CW_90).get()
      val baos = new ByteArrayOutputStream()
      ImageIO.write(rotatedBufferedImage, "jpg", baos)
      baos.flush()
      val rotatedImage = Image(originalImage.name, baos.toByteArray)
      baos.close()
      rotatedImage
    }(Flipper.ec)
  }
}

object Flipper {
  val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(15))
}
