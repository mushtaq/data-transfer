package tmt.wavefront

import akka.http.scaladsl.model.DateTime
import tmt.common.ActorConfigs
import tmt.common.models.{PerSecMetric, CumulativeMetric, ImageMetric}
import tmt.media.server.ImageWriteService

import scala.concurrent.duration.DurationInt

class ImageTransformations(
  imageSourceService: ImageSourceService,
  imageWriteService: ImageWriteService,
  actorConfigs: ActorConfigs) {

  import actorConfigs._

  val images = imageSourceService.images

  val filteredImages = images.filter(_.name.contains("9"))

  val copiedImages = images.mapAsync(1) { image =>
    imageWriteService.copyImage(image).map(_ => image)
  }

  val imageMetrics = images.map(image => ImageMetric(image.name, image.size, DateTime.now.clicks))
  val cumulativeMetrics = imageMetrics.scan(CumulativeMetric("", 0, 0, 0, 0))(_ + _)
  val perSecMetrics = imageMetrics.groupedWithin(10000, 1.second).map(PerSecMetric.fromImageMetrics)
}
