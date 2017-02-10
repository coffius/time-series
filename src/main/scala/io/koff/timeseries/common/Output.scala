package io.koff.timeseries.common

/**
  * Created by coffius on 10.02.2017.
  */
case class Output(timestamp: Long,
                  value: Double,
                  numOfMeasurements: Long,
                  rollingSum: Double,
                  minValue: Double,
                  maxValue: Double)