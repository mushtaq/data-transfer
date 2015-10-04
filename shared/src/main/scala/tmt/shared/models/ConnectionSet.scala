package tmt.shared.models

case class ConnectionSet(connections: Map[String, Set[String]]) {
  def addConnection(serverName: String, topic: String) = copy {
    val currentTopics = getTopics(serverName)
    connections + (serverName -> (currentTopics + topic))
  }

  def removeConnection(serverName: String, topic: String) = copy {
    val currentTopics = getTopics(serverName)
    connections + (serverName -> (currentTopics - topic))
  }

  def flatConnections = {
    for {
      (serverName, topics) <- connections.toSeq
      topic <- topics
    } yield (serverName, topic)
  }.sorted

  def pruneBy(onlineRoles: Set[String]) = ConnectionSet {
    connections.collect { case (role, serverNames) if onlineRoles.contains(role) =>
      role -> serverNames.filter(onlineRoles)
    }
  }

  def getTopics(serverName: String) = connections.getOrElse(serverName, Set.empty)
}

object ConnectionSet {
  def empty = ConnectionSet(Map.empty)
}
