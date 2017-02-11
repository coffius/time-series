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
    val outputBuffer = new Array[ProcessedElement](bufferSize)

    while (copied > 0) {
      calculate(bufferSize, copied, rollingWindow, buffer, outputBuffer, onResult)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)
    }
  }

  def calcFirstOutput(startPos: Int, rollingWindow: Long, data: Array[TimeRecord]): ProcessedElement = {
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

    val output = Output(
      timestamp = mainRecord.timestamp,
      value = mainRecord.value,
      numOfMeasurements =  number,
      rollingSum = sum,
      minValue = minVal,
      maxValue = maxVal
    )

    ProcessedElement(output, Range(currPos, startPos))
  }

  private def calcOtherElem(startPos: Int,
                            rollingWindow: Long,
                            data: Array[TimeRecord],
                            prevElem: ProcessedElement): ProcessedElement = {
    val mainRecord = data(startPos)
    val currPos = prevElem.range.left - 1
    var num = prevElem.output.numOfMeasurements - 1
    var sum = prevElem.output.rollingSum - prevElem.output.value

    while (
      currPos >= 0
        && data(currPos) != null
        && mainRecord.timestamp - data(currPos).timestamp <= rollingWindow
    ) {
      val elem = data(currPos)
      num += 1
      sum += elem.value
    }

    val minVal = ???
    val maxVal = ???
    ???
  }

  private def calculate(startPos: Int,
                        length: Int,
                        rollingWindow: Long,
                        data: Array[TimeRecord],
                        outputBuffer: Array[ProcessedElement],
                        onResult: Output => Unit): Unit = {
    val endPos = startPos + length
    var currPos = endPos - 1
    var outputIndex = outputBuffer.length - 1
    var prevElem = calcFirstOutput(startPos, rollingWindow, data)
    outputBuffer(outputIndex) = prevElem
    while (currPos >= startPos) {
      prevElem = calcOtherElem(currPos, rollingWindow, data, prevElem)
      outputBuffer(outputIndex) = prevElem
      currPos -= 1
      outputIndex -= 1
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
