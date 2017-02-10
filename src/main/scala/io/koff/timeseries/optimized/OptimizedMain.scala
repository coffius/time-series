package io.koff.timeseries.optimized

import java.io.File

import io.koff.timeseries.common.Output

/**
  * With optimizations
  */
object OptimizedMain {
  private val defaultFileName = "./data/big_data_100M.txt"
  private val bufferSize = 10000
  private val rollingWindow = 60

  def main(args: Array[String]): Unit = {
    val file = new File(defaultFileName)

    OptimizedCalculator.calculate(file, bufferSize, rollingWindow, identity[Output])
  }
}
