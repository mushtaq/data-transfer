package tmt.media.client

import org.scalatest.{BeforeAndAfterAll, FunSuite, MustMatchers}
import tmt.wavefront.{Role, MainStreaming}

class PipelineTest extends FunSuite with MustMatchers with BeforeAndAfterAll {

  val runningServers = Role.values.map(role => new MainStreaming(role.entryName, "dev"))

  test("run") {
    Thread.sleep(30000)
  }

  override protected def afterAll() = {
    runningServers.foreach(_.stop())
  }
}
