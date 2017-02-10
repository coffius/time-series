package io.koff.timeseries

import java.io.File

import io.koff.timeseries.common.Output
import io.koff.timeseries.naive.NaiveCalculator
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable

class NaiveCalculatorSpec extends FreeSpec with Matchers {

  "should check calculations" in {
    val accum = mutable.ArrayBuilder.make[Output]()
    NaiveCalculator.calculate(new File("data/test_data.txt"), 100, 60, accum += _)

    accum.result().toSeq should contain theSameElementsInOrderAs testOutput
  }
}
