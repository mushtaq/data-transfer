package actors

import akka.actor.{Actor, Props}
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.Replicator._
import tmt.common.Keys
import tmt.shared.models.ConnectionDataSet

class ConnectionStore extends Actor {

  var connections = Map.empty[String, Set[String]]

  val replicator = DistributedData(context.system).replicator

  override def preStart() = {
    replicator ! Subscribe(Keys.Connections, self)
  }

  def receive = {
    case g@Changed(Keys.Connections)    => connections = g.get(Keys.Connections).entries
    case ConnectionStore.GetConnections => sender() ! ConnectionDataSet(connections)
  }
}

object ConnectionStore {
  case object GetConnections

  def props() = Props(new ConnectionStore)
}
