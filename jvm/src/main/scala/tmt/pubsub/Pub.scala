package tmt.pubsub

import akka.actor.Actor
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import tmt.common.models.Image

class Pub extends Actor {

  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case image: Image =>
      println("^^^^^^^^^^^^^^^^^" + image.name)
      mediator ! Publish("image-source", image)
    case "done"       =>
      println(s"pub stream closed")
  }
}
