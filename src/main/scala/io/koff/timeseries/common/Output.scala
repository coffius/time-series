package io.koff.timeseries.common

import java.text.DecimalFormat
import Output._

/**
  * Output element.
  * @param timestamp  number of seconds since beginning of epoch at which rolling window ends
  * @param value  measurement of price ratio at time T
  * @param numOfMeasurements number of measurements in the window.
  * @param rollingSum a rolling sum of measurements in the window
  * @param minValue minimum price ratio in the window.
  * @param maxValue maximum price ratio the window.
  */
case class Output(timestamp: Long,
                  value: Double,
                  numOfMeasurements: Int,
                  rollingSum: Double,
                  minValue: Double,
                  maxValue: Double) {

  /**
    * Prints info from an output to string
    * @return formatted string
    */
  def toPrintString: String = {
    val rollingSumStr = format.format(rollingSum)
    f"$timestamp%d $value%.5f $numOfMeasurements%-3d $rollingSumStr%-9s $minValue%.5f $maxValue%.5f"
  }
}

object Output {
  private val format = new DecimalFormat("#.#####")
}