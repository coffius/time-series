package io.koff.timeseries

import java.io.File

import io.koff.timeseries.common.Output
import io.koff.timeseries.optimized.OptimizedCalculator
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable

class OptimizedCalculatorSpec extends FreeSpec with Matchers {

  "should check calculations" in {
    val accum = mutable.ArrayBuilder.make[Output]()
    OptimizedCalculator.calculate(new File("data/test_data.txt"), 100, 60, accum += _)

    accum.result().toSeq should contain theSameElementsInOrderAs testOutput
  }
}
