package io.koff.timeseries.parallel

import io.koff.timeseries.common.Output

/**
  * Output with an index range
  */
case class ProcessedElement(output: Output, range: Range)
