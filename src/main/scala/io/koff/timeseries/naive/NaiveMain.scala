package io.koff.timeseries.naive

import java.io.File

import io.koff.timeseries.common.Output


/**
  * As simple as possible. It is the very first attempt in order to understand possible challenges
  */
object NaiveMain {
  private val defaultFileName = "./data/big_data_100M.txt"
  private val bufferSize = 10000
  private val rollingWindow = 60

  def main(args: Array[String]): Unit = {
    val file = new File(defaultFileName)

    NaiveCalculator.calculate(file, bufferSize, rollingWindow, identity[Output])
  }
}
