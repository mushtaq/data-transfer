package tmt.clients

import java.net.InetSocketAddress

import akka.http.scaladsl.model._
import akka.stream.scaladsl.{FlattenStrategy, Source, Flow}
import tmt.app.{ActorConfigs, AppSettings}
import tmt.library.InetSocketAddressExtensions.RichInetSocketAddress
import tmt.library.ResponseExtensions.RichResponse
import tmt.marshalling.BFormat

import scala.concurrent.duration.DurationInt
import scala.util.Success

class ProducingClient(address: InetSocketAddress, actorConfigs: ActorConfigs, settings: AppSettings) {
  import actorConfigs._

  val flow: Flow[String, (MessageEntity, Uri), Unit] = Flow[String]
    .map(address.absoluteUri)
    .map(uri => HttpRequest(uri = uri) -> uri)
    .via(settings.superPool)
    .collect {  case (Success(response), uri) if response.status == StatusCodes.OK =>
      response.multicastEntity -> uri
    }

  def request[T: BFormat](routeName: String) = Source(List(routeName))
    .via(flow)
    .map { case (entity, _) =>  entity.dataBytes }
    .flatten(FlattenStrategy.concat)
    .map(BFormat[T].read)
}

class ProducingClientFactory(actorConfigs: ActorConfigs, settings: AppSettings) {
  def make(address: InetSocketAddress) = new ProducingClient(address, actorConfigs, settings)
}

class ConsumingClient(address: InetSocketAddress, actorConfigs: ActorConfigs, settings: AppSettings) {
  import actorConfigs._

  val flow: Flow[(MessageEntity, Uri), HttpResponse, Any] = Flow[(MessageEntity, Uri)]
    .collect { case (entity, uri) =>
      HttpRequest(uri = address.update(uri), method = HttpMethods.POST, entity = entity) -> address.update(uri)
    }
    .via(settings.superPool)
    .mapAsync(1) { case (Success(response), name) =>
      response.toStrict(1.second)
    }

}

class ConsumingClientFactory(actorConfigs: ActorConfigs, settings: AppSettings) {
  def make(address: InetSocketAddress) = new ConsumingClient(address, actorConfigs, settings)
}
