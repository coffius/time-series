package io.koff.timeseries.optimized

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.io.Source

object OptimizedCalculator {

  def calculate(file: File, bufferSize: Int, rollingWindow: Long, onResult: Output => Unit): Unit = {
    val dataSource = Source.fromFile(file)
    val recordIter = dataSource.getLines().map(toTimeRecord)

    val buffer = new Array[TimeRecord](2 * bufferSize)
    var copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)

    while (copied > 0) {
      calculate(bufferSize, bufferSize + copied, rollingWindow, buffer, onResult)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)
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

  def copyToArray[T](src: Iterator[T], dst: Array[T], start: Int, len: Int)(): Int = {
    var i = start
    val end = start + len
    while (i < end && src.hasNext) {
      dst(i) = src.next()
      i += 1
    }
    i - start
  }
}
