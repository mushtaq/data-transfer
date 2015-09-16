package tmt.wavefront

import tmt.common.models.{CumulativeMetric, ImageMetric, PerSecMetric}

import scala.concurrent.duration.DurationInt

class MetricsTransformations(itemSourceService: ItemSourceService) {
  lazy val imageMetrics = itemSourceService.getItems[ImageMetric](Routes.Metrics)
  lazy val cumulativeMetrics = imageMetrics.scan(CumulativeMetric("", 0, 0, 0, 0))(_ + _)
  lazy val perSecMetrics = imageMetrics.groupedWithin(50000, 5.second).map(PerSecMetric.fromImageMetrics)
}
