package tmt.wavefront

import tmt.media.MediaAssembly

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  val Array(role, env) = args
  RunningServerFactory.create(role, env)
}

class RunningServer(val assembly: MediaAssembly) {
  val server = assembly.serverFactory.make()
  val binding = Await.result(server.run(), 1.second)
  
  def stop() = {
    Await.result(binding.unbind(), 1.second)
    assembly.system.shutdown()
  }
}

object RunningServerFactory {
  def create(mediaAssembly: MediaAssembly) = {
    new RunningServer(mediaAssembly)
  }

  def create(role: String, env: String) = {
    new RunningServer(new MediaAssembly(role, env))
  }
}
