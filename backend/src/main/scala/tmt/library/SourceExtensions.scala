package tmt.library

import akka.stream.Materializer
import akka.stream.scaladsl._
import org.reactivestreams.Publisher

import scala.concurrent.duration.FiniteDuration

object SourceExtensions {

  implicit class RichSource[Out, Mat](val source: Source[Out, Mat]) extends AnyVal {

    def zip[Out2, Mat2](other: Source[Out2, Mat2]) = Source(source) { implicit b => src =>
      import FlowGraph.Implicits._

      val zipper = b.add(Zip[Out, Out2]())

      src ~> zipper.in0
      other ~> zipper.in1

      zipper.out
    }

    def zipWithIndex = zip(Source(() => Iterator.from(0)))

    def throttle(duration: FiniteDuration) = throttleBy(Sources.ticks(duration))
    def throttleBy[Out2, Mat2](other: Source[Out2, Mat2]) = source.zip(other).map(_._1)

    def multicast(implicit mat: Materializer) = Source(source.runWith(Sink.fanoutPublisher(2, 2)))

    def hotUnicast(implicit mat: Materializer) = hot(Sink.publisher)
    def hotMulticast(implicit mat: Materializer) = hot(Sink.fanoutPublisher(2, 2))

    def hot(sink: Sink[Out, Publisher[Out]])(implicit mat: Materializer) = {
      val (actorRef, hotSource) = Connector.coupling(sink)
      source.runForeach(x => actorRef ! x)
      hotSource
    }

  }

  def merge[Out, Mat](sources: Seq[Source[Out, Mat]]) =
    if(sources.isEmpty)
      Source.empty[Out]
    else {
      Source() { implicit b =>
        import FlowGraph.Implicits._

        val merge = b.add(Merge[Out](sources.length))

        sources.zipWithIndex.foreach { case (source, index) => source ~> merge.in(index) }

        merge.out
      }
    }

}
