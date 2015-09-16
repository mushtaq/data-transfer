package tmt

import java.io.PrintWriter
import java.net.InetSocketAddress
import java.util.Calendar
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Source
import tmt.common.ActorConfigs
import tmt.common.models.{PerSecMetric, ImageMetric}
import tmt.library.SourceExtensions
import tmt.marshalling.BFormat
import tmt.media.MediaAssembly
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import tmt.library.SourceExtensions._

object StatisticsMonitor {
  val testAssembly = new MediaAssembly("stats-monitor")

  val inetSocketAddresses = List(new InetSocketAddress("127.0.0.1", 8001), new InetSocketAddress("127.0.0.1", 8002), new InetSocketAddress("127.0.0.1", 8004))
  import testAssembly.actorConfigs._


  private def inetAddress(address: InetSocketAddress) = s"${address.getHostName}:${address.getPort}"

  def statsOfNode[T: BFormat](address: InetSocketAddress) = {
    val response = Await.result(Http().singleRequest(HttpRequest(uri = s"http://${inetAddress(address)}/metrics")), 10.seconds)
    address -> response.entity.dataBytes.map(BFormat[T].read).multicast
  }

  def statsStreams[T: BFormat] = inetSocketAddresses.map { address =>
    statsOfNode[T](address)
  }.toMap

  def main(args: Array[String]) {
    println("starting..")
    val statsPerNode = statsStreams[ImageMetric]
    val throughputPerNode = statsPerNode.map { imageMetricsWithAddress =>
      imageMetricsWithAddress._1 -> new StatsCalculator(imageMetricsWithAddress._2, imageMetricsWithAddress._1).throughput
    }
    new LatencyCalculator(statsPerNode.toList, testAssembly.actorConfigs).latency
    throughputPerNode.map { throughputPerSec =>
      val statsWriter = new PrintWriter(s"${inetAddress(throughputPerSec._1)}.csv")

      throughputPerSec._2
        .dropWithin(1.minute)
        .takeWithin(2.minute)
        .map { metric =>
          Calendar.getInstance.getTime.toString + " , " + metric.count / 5 + " , " + metric.size / 5 + "\n"
        }
        .runForeach(metric => { println(metric); statsWriter.append(metric); statsWriter.flush() })
    }



    Thread.sleep(1000 * 60 * 5)
    testAssembly.actorConfigs.system.shutdown()
  }
}

class StatsCalculator(source: Source[ImageMetric, Any], address: InetSocketAddress) {
  val max_images = 10000
  val duration = 5.seconds

  def throughput = {
    source.groupedWithin(max_images, duration).map(PerSecMetric.fromImageMetrics)
  }
}

class LatencyCalculator(sources: List[(InetSocketAddress, Source[ImageMetric, Any])], actorConfigs: ActorConfigs) {
  def transform = {
    sources.map(x => x._2.map(metric => metric -> x._1))
  }

  def latency: Source[(String, Long), Any] = {
    import actorConfigs._
    println("latency...")
    val merge1 = SourceExtensions.merge(transform).groupBy(_._1.name).multicast

    val map = merge1.map { grouping =>
      val take = grouping._2.takeWithin(10.second).take(sources.length)
      val minimum = Await.result(take.runFold[Long](Long.MaxValue) { (a, b) =>
        if (b._1.timestamp < a) b._1.timestamp
        else a
      }, 1.seconds)

      val maximum = Await.result(take.runFold[Long](0) { (a, b) =>
        if (b._1.timestamp > a) b._1.timestamp
        else a
      }, 1.seconds)
      grouping._1 -> (maximum - minimum)
    }
    map.runForeach(println)
    map
  }
}

