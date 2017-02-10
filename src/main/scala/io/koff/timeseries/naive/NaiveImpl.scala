package io.koff.timeseries.naive

import java.io.File
import java.util

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


/**
  * As simple as possible. It is the very first attempt in order to understand possible challenges
  */
object NaiveImpl {
  private val defaultFileName = "test_data.txt"
  private val bufferSize = 100
  private val rollingWindow = 60

  def calcOutput(startPos: Int, data: Array[TimeRecord]): Output = {
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

  def calculateAndPrint(startPos: Int, endPos: Int, data: Array[TimeRecord]): Unit = {
    var currPos = startPos

    while (currPos < endPos) {
      val output = calcOutput(currPos, data)
      println(output)
      currPos += 1
    }
  }

  def main(args: Array[String]): Unit = {
    val file = new File(defaultFileName)
    val dataSource = Source.fromFile(file)
    val recordIter = dataSource.getLines().map(toTimeRecord)

    val buffer = new Array[TimeRecord](2 * bufferSize)
    var newData = copyToArray(recordIter, 0, bufferSize)
    while (newData.length > 0) {
      Array.copy(newData, 0, buffer, bufferSize, newData.length)
      calculateAndPrint(bufferSize, bufferSize + newData.length, buffer)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      newData = copyToArray(recordIter, 0, bufferSize)
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
