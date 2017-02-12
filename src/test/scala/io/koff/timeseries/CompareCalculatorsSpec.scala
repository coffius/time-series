package io.koff.timeseries

import java.io.File

import io.koff.timeseries.naive.NaiveCalculator
import io.koff.timeseries.optimized.OptimizedCalculator
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable

/**
  * All implementations should return the same result
  */
class CompareCalculatorsSpec extends FreeSpec with Matchers {
  private val file = new File("data/data_scala.txt")
  private val bufferSize = 100
  private val rollingWindow = 60

  "check outputs of both implementations" in {
    val optimizedAccum = mutable.ArrayBuilder.make[String]()
    OptimizedCalculator.calculate(file, bufferSize, rollingWindow, optimizedAccum += _.toPrintString)

    val naiveAccum = mutable.ArrayBuilder.make[String]()
    NaiveCalculator.calculate(file, bufferSize, rollingWindow, naiveAccum += _.toPrintString)

    val optimized = optimizedAccum.result().toSeq
    val naive = naiveAccum.result().toSeq

    naive should contain theSameElementsInOrderAs optimized
  }
}
