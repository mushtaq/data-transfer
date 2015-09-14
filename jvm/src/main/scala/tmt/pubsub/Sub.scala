package tmt.pubsub

import akka.actor.Actor
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{SubscribeAck, Subscribe}
import tmt.common.models.Image

class Sub extends Actor {

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("image-source", self)

  def receive = {
    case SubscribeAck(Subscribe("image-source", None, `self`)) ⇒
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
      context become ready
  }

  def ready: Receive = {
    case image: Image => println("*************" +  image.name)
  }
}
