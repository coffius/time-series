package io.koff.timeseries.naive

/**
  * Created by coffius on 10.02.2017.
  */
case class Output(timestamp: Long,
                  value: Double,
                  numOfMeasurments: Long,
                  rollingSum: Double,
                  minValue: Double,
                  maxValue: Double)