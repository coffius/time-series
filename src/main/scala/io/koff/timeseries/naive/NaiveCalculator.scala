package io.koff.timeseries.naive

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.collection.mutable
import scala.io.Source

object NaiveCalculator {

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

  def calcOutput(startPos: Int, rollingWindow: Long, data: Array[TimeRecord]): Output = {
    val mainRecord = data(startPos)
    var currPos = startPos
    var minVal = Double.MaxValue
    var maxVal = Double.MinValue
    var sum = 0.0
    var number = 0

    while (
      currPos >= 0
        && data(currPos) != null
        && mainRecord.timestamp - data(currPos).timestamp <= rollingWindow
    ) {
      val currRecord = data(currPos)
      minVal = if(currRecord.value < minVal) currRecord.value else minVal
      maxVal = if(currRecord.value > maxVal) currRecord.value else minVal
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

  private def calculate(startPos: Int,
                        endPos: Int,
                        rollingWindow: Long,
                        data: Array[TimeRecord],
                        onResult: Output => Unit): Unit = {
    var currPos = startPos

    while (currPos < endPos) {
      val output = calcOutput(currPos, rollingWindow, data)
      onResult(output)
      currPos += 1
    }
  }

  private def toTimeRecord(str: String) = str match {
    case TimeRecord(record) => record
    case invalidStr => throw new IllegalStateException(s"invalid format of string: $invalidStr")
  }

  def copyToArray(iter: Iterator[TimeRecord], start: Int, len: Int): Array[TimeRecord] = {
    val arrayBuider: mutable.ArrayBuilder[TimeRecord] = mutable.ArrayBuilder.make()
    var i = start
    val end = start + len
    while (i < end && iter.hasNext) {
      arrayBuider += iter.next()
      i += 1
    }
    arrayBuider.result()
  }
}