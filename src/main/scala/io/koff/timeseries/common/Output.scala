package io.koff.timeseries.common

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
    f"$timestamp%d $value%.5f $numOfMeasurements%d $rollingSum%.5f $minValue%.5f $maxValue%.5f"
  }
}