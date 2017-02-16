package io.koff.timeseries

import java.io.File

import io.koff.timeseries.common.Output
import io.koff.timeseries.parallel.ParallelCalculator

/**
  * The main class of the application
  */
object PerformanceMain {
  private val BufferSize = 1000
  private val RollingWindow = 60
  def main(args: Array[String]): Unit = {
    val inputFile = if(args.length > 0) {
      val file = new File(args(0))
      if(!file.exists() || !file.isFile) {
        throw new IllegalArgumentException(s"Can't find a file with the name: ${file.getAbsolutePath}")
      } else {
        file
      }
    } else {
      throw new IllegalArgumentException("Please provide a filename for analysis")
    }

    println("calculation has been started")
    ParallelCalculator.calculate(inputFile, BufferSize, RollingWindow, identity[Output])
  }
}
