package io.koff.timeseries.common

import java.text.DecimalFormat

/**
  * Created by coffius on 10.02.2017.
  */
case class Output(timestamp: Long,
                  value: Double,
                  numOfMeasurements: Int,
                  rollingSum: Double,
                  minValue: Double,
                  maxValue: Double) {

  def toPrintString: String = {
    val rollingSumStr =  new DecimalFormat("#.#####").format(rollingSum)
    f"$timestamp%d $value%.5f $numOfMeasurements%-3d $rollingSumStr%-9s $minValue%.5f $maxValue%.5f"
  }
}