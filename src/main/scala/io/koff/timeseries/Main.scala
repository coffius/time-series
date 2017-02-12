package io.koff.timeseries

import java.io.File

import io.koff.timeseries.optimized.OptimizedCalculator

/**
  * Created by coffius on 12.02.2017.
  */
object Main {
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

    println("T          V       N   RS        MinV    MaxV")
    println("------------------------------------------------")
    OptimizedCalculator.calculate(inputFile, BufferSize, RollingWindow, out => println(out.toPrintString))
  }
}
