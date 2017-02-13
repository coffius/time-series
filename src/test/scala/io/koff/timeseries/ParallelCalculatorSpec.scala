package io.koff.timeseries

import java.io.File

import io.koff.timeseries.common.Output
import io.koff.timeseries.parallel.ParallelCalculator
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable

/**
  * Spec for OptimizedCalculator
  */
class ParallelCalculatorSpec extends FreeSpec with Matchers {

  "should check calculations" in {
    val accum = mutable.ArrayBuilder.make[Output]()
    ParallelCalculator.calculate(new File("data/test_data.txt"), 4, 60, accum += _)

    accum.result().toSeq.map(_.toPrintString) should contain theSameElementsInOrderAs testOutput.map(_.toPrintString)
  }
}
