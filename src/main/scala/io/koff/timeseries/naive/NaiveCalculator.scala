package io.koff.timeseries.naive

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.collection.mutable
import scala.io.Source

/**
  * Simple and straightforward implementation of calculations
  */
object NaiveCalculator {

  /**
    * Makes calculations using input data from `file` and executes `onResult` func for each calculated output
    * @param file the file with input data
    * @param bufferSize number of elements that are read from file at a time
    * @param rollingWindow length of a rolling window
    * @param onResult the function which is executed for each output
    */
  def calculate(file: File, bufferSize: Int, rollingWindow: Long, onResult: Output => Unit): Unit = {
    val dataSource = Source.fromFile(file)
    val recordIter = dataSource.getLines().map(toTimeRecord)

    val buffer = new Array[TimeRecord](2 * bufferSize)
    var newData = copyToArray(recordIter, 0, bufferSize)

    while (newData.length > 0) {
      Array.copy(newData, 0, buffer, bufferSize, newData.length)
      calculate(bufferSize, bufferSize + newData.length, rollingWindow, buffer, onResult)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      newData = copyToArray(recordIter, 0, bufferSize)
    }
  }

  /**
    * Makes calculations for one record
    * @param index the index of the record
    * @param rollingWindow length of a rolling window
    * @param data array with input data
    * @return calculated output
    */
  private def calcOutput(index: Int, rollingWindow: Long, data: Array[TimeRecord]): Output = {
    val mainRecord = data(index)
    var currPos = index
    var minVal = Double.MaxValue
    var maxVal = Double.MinValue
    var sum = 0.0
    var number = 0

    // finds min/max values and calculates sum and number of records
    // while records are in rolling window
    // and until the end of data in input array
    while (
      currPos >= 0
        && data(currPos) != null
        && mainRecord.timestamp - data(currPos).timestamp <= rollingWindow
    ) {
      val currRecord = data(currPos)
      minVal = if(currRecord.value < minVal) currRecord.value else minVal
      maxVal = if(currRecord.value > maxVal) currRecord.value else maxVal
      sum += currRecord.value
      number += 1
      currPos -= 1
    }

    Output(
      timestamp = mainRecord.timestamp,
      value = mainRecord.value,
      numOfMeasurements =  number,
      rollingSum = sum,
      minValue = minVal,
      maxValue = maxVal
    )
  }

  /**
    * Calculates output for each record in input array
    * @param startPos the start position in input array
    * @param endPos the last position in input array
    * @param rollingWindow length of a rolling window
    * @param data the input array
    * @param onResult the function which is executed for every output
    */
  private def calculate(startPos: Int,
                        endPos: Int,
                        rollingWindow: Long,
                        data: Array[TimeRecord],
                        onResult: Output => Unit): Unit = {
    var currPos = startPos

    // makes calculations for each record in input array
    while (currPos < endPos) {
      val output = calcOutput(currPos, rollingWindow, data)
      onResult(output)
      currPos += 1
    }
  }

  /**
    * Converts a string in TimeRecord if it is possible and throws IllegalStateException if it is not
    * @param str the string to convert
    * @return record
    * @throws IllegalStateException if the string has a wrong format
    */
  private def toTimeRecord(str: String): TimeRecord = str match {
    case TimeRecord(record) => record
    case invalidStr => throw new IllegalStateException(s"invalid format of string: $invalidStr")
  }

  /**
    * Copies values from a iterator to array
    */
  def copyToArray(iter: Iterator[TimeRecord], start: Int, len: Int): Array[TimeRecord] = {
    val arrayBuilder: mutable.ArrayBuilder[TimeRecord] = mutable.ArrayBuilder.make()
    var i = start
    val end = start + len
    while (i < end && iter.hasNext) {
      arrayBuilder += iter.next()
      i += 1
    }
    arrayBuilder.result()
  }
}
