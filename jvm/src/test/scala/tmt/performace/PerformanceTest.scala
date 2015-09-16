package tmt.performace

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import tmt.wavefront.RunningServerFactory

class PerformanceTest extends FunSuite with BeforeAndAfterAll {
  val testParameters = new TestParameters()
  testParameters.setFileSizeRange(0, 200000)

  val roles = Seq("image-source", "image-copy", "metrics-per-image")
  val runningServers = roles.map { role =>
    val testAssembly = new PerfMediaAssembly(role, testParameters)
    RunningServerFactory.create(testAssembly)
  }

  val Seq(source, imageCopy, metricsPerImage) = runningServers.map(_.assembly.binding)

  test("test 1") {
    Thread.sleep(1000000)
  }

  override protected def afterAll() = {
    runningServers.foreach(_.stop())

  }
}