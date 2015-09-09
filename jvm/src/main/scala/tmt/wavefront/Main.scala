package tmt.wavefront

import tmt.media.MediaAssembly

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  val Array(role, env) = args
  new RunningServer(role, env)
}

class RunningServer(role: String, env: String) {
  val assembly = new MediaAssembly(role, env)
  val server = assembly.serverFactory.make()
  val binding = Await.result(server.run(), 1.second)
  
  def stop() = {
    Await.result(binding.unbind(), 1.second)
    assembly.system.shutdown()
  }
}
