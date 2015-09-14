package tmt.pubsub

import tmt.media.MediaAssembly
import tmt.wavefront.Role

object MainPubSub extends App {
  val Array(role, env) = args
  new MainPubSub(role, env)
}

class MainPubSub(role: String, env: String) {
  val assembly = new MediaAssembly(role, env)
  import assembly._
  pubSubFactory.start(Role.withName(appSettings.topology.role))

  def stop() = {
    system.terminate()
  }
}
