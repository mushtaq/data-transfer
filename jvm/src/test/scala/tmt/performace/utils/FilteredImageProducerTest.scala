package tmt.performace.utils

import org.scalatest.{FunSuite, MustMatchers}
import tmt.performace.{PerfMediaAssembly, TestParameters}

class FilteredImageProducerTest extends FunSuite with MustMatchers {

  test("should get files having sizes between specified range") {
    val testParameters = new TestParameters()
    testParameters.setFileSizeRange(0, 5)
    val testAssembly = new PerfMediaAssembly("application", testParameters)
    val filteredFiles = testAssembly.producer.files("src/test/resources/images/")

    filteredFiles.next().getName mustEqual "4bytes.txt"
    filteredFiles.next().getName mustEqual "4bytes.txt"
  }
}
