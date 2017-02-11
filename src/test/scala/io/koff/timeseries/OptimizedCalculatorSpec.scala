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

  "should check array copying" in {
    val src: Seq[Int] = 1 to 10
    val iterator = src.iterator
    val dst = new Array[Int](8)
    val copied = OptimizedCalculator.copyToArray(iterator, dst, 2, 5)
    copied shouldBe 5
    dst should contain theSameElementsInOrderAs Array(0, 0, 1, 2, 3, 4, 5, 0)
  }
}
