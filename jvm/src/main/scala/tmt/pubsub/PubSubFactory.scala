package tmt.pubsub

import akka.actor.Props
import akka.stream.scaladsl.Sink
import tmt.common.ActorConfigs
import tmt.media.server.ImageReadService
import tmt.wavefront.Role

class PubSubFactory(
  actorConfigs: ActorConfigs,
  imageReadService: ImageReadService,
  pubProps: Props,
  subProps: Props) {

  import actorConfigs._

  def start(role: Role) = role match {
    case Role.ImageSource     =>
      val pubRef = system.actorOf(pubProps)
      imageReadService.sendImages.runWith(Sink.actorRef(pubRef, "done"))
    case Role.MetricsPerImage =>
      system.actorOf(subProps)
  }
}
