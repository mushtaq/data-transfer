package tmt.media.client

import java.net.InetSocketAddress

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, MessageEntity}
import tmt.common.ActorConfigs
import tmt.library.InetSocketAddressExtensions.RichInetSocketAddress

import scala.concurrent.duration.DurationInt

class SimpleTransfer(source: InetSocketAddress, destination: InetSocketAddress, actorConfigs: ActorConfigs) {

  import actorConfigs._

  def singleTransfer(relativeUri: String) = {
    val sourceRequest = HttpRequest(uri = source.absoluteUri(relativeUri))
    val sourceResponse = Http().singleRequest(sourceRequest)

    sourceResponse.flatMap { resp =>
      val sourceEntity = resp.entity.asInstanceOf[MessageEntity]
      val destinationRequest = HttpRequest(uri = destination.absoluteUri(relativeUri), method = HttpMethods.POST, entity = sourceEntity)
      Http().singleRequest(destinationRequest).flatMap(_.toStrict(1.second))
    }
  }
}

class SimpleTransferFactory(actorConfigs: ActorConfigs) {
  def make(source: InetSocketAddress, destination: InetSocketAddress) = new SimpleTransfer(source, destination, actorConfigs)
}
