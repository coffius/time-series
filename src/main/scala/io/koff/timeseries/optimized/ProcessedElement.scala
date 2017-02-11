package io.koff.timeseries.optimized

import io.koff.timeseries.common.Output

/**
  * Output with an index range
  */
case class ProcessedElement(output: Output, range: Range)
