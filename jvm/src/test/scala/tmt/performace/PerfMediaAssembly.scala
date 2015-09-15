package tmt.performace

import tmt.common.Producer
import tmt.media.MediaAssembly
import tmt.performace.utils.FilteredImageProducer

class TestParameters {
  var minFileSize = 0
  var maxFileSize = 0

  def setFileSizeRange(_minFileSize: Int, _maxFileSize: Int) = {
    minFileSize = _minFileSize
    maxFileSize = _maxFileSize
  }
}

class PerfMediaAssembly(name: String, perfTestParameters: TestParameters, env: String = "dev") extends MediaAssembly(name, "dev") {

  override lazy val producer: Producer = new FilteredImageProducer(appSettings, perfTestParameters.minFileSize, perfTestParameters.maxFileSize)
}
