package tmt.wavefront

import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.server.Directives._
import tmt.common.models.ImageMetric
import tmt.marshalling.BinaryMarshallers
import tmt.media.server.{ImageReadService, MediaRoute}

class RouteInstances(
  routeFactory: RouteFactory,
  imageTransformations: ImageTransformations,
  metricsTransformations: MetricsTransformations,
  imageReadService: ImageReadService,
  mediaRoute: MediaRoute
) extends BinaryMarshallers {

  def find(role: Role) = role match {
    case Role.ImageSource =>
      val imageSource = imageReadService.sendImages
      routeFactory.make(
      Routes.Images, imageSource.map(x => {Thread.sleep(20); println(s"$role: serving image ${x.name} of size ${x.size}"); x})
    ) ~
    routeFactory.make(
      Routes.Metrics, imageSource.map(image => {Thread.sleep(20); ImageMetric(image.name, image.size, DateTime.now.clicks) } )
    )

    case Role.ImageCopy   => routeFactory.make(
      Routes.Images, imageTransformations.copiedImages.map(x => {println(s"$role: serving image ${x.name}"); x})
    ) ~
    routeFactory.make(
      Routes.Metrics, imageTransformations.imageMetrics.map(x => {println(s"$role: serving metric $x"); x})
    )

    case Role.ImageFilter => routeFactory.make(
      Routes.Images, imageTransformations.filteredImages.map(x => {println(s"$role: serving image ${x.name}"); x})
    )

    case Role.MetricsPerImage => routeFactory.make(
      Routes.Metrics, imageTransformations.imageMetrics.map(x => {println(s"$role: serving metric $x"); x})
    )

    case Role.MetricsAgg =>
      routeFactory.websocket(
        Routes.MetricsCumulative, metricsTransformations.cumulativeMetrics.map(x => {println(s"$role: serving metric $x"); x})
      ) ~ routeFactory.websocket(
        Routes.MetricsPerSec, metricsTransformations.perSecMetrics.map(x => {println(s"$role: serving metric $x"); x})
      ) ~ mediaRoute.route
  }
}
