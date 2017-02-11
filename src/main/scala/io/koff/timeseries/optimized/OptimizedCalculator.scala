package io.koff.timeseries.optimized

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.collection.{SeqView, mutable}
import scala.io.Source

object OptimizedCalculator {
  type DataView = mutable.IndexedSeqView[TimeRecord, Array[TimeRecord]]

  def calculate(file: File, bufferSize: Int, rollingWindow: Long, onResult: Output => Unit): Unit = {
    val dataSource = Source.fromFile(file)
    val recordIter = dataSource.getLines().map(toTimeRecord)

    val buffer = new Array[TimeRecord](2 * bufferSize)
    var copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)

    while (copied > 0) {
      calculate(bufferSize, copied, rollingWindow, buffer, onResult)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)
    }
  }

  def calcFirstElement(currPos: Int, rollingWindow: Long, data: DataView): ProcessedElement = {
    val mainRecord  = data.last
    val withFilters = data.reverse.takeWhile(recordFilter(mainRecord, rollingWindow))
    val mapped = withFilters.map(_.value)
    val length = withFilters.length

    val output = Output(
      timestamp         = mainRecord.timestamp,
      value             = mainRecord.value,
      numOfMeasurements = length,
      rollingSum        = mapped.sum,
      minValue          = mapped.min,
      maxValue          = mapped.max
    )

    ProcessedElement(output, Range(currPos - length + 1, currPos))
  }

  private def calcOtherElem(rollingWindow: Long,
                            data: Array[TimeRecord],
                            prevElem: ProcessedElement): ProcessedElement = {
    val currPos = prevElem.range.right - 1
    val mainRecord = data(currPos)
    val leftRange = data.slice(0, prevElem.range.left).reverse
    val withFilters = leftRange.takeWhile(recordFilter(mainRecord, rollingWindow))
    val mapped = withFilters.map(_.value)
    val filteredLength = withFilters.length
    val num = filteredLength + prevElem.output.numOfMeasurements - 1
    val sum = mapped.sum + prevElem.output.rollingSum - prevElem.output.value

    val forWorst = data.slice(0, currPos + 1).reverse.takeWhile(recordFilter(mainRecord, rollingWindow)).map(_.value)
    val minVal = if(prevElem.output.minValue == prevElem.output.value) {
      if(forWorst.nonEmpty) {
        forWorst.min
      } else {
        mainRecord.value
      }
    } else {
      if(filteredLength <= 0) {
        prevElem.output.minValue
      } else {
        math.min(mapped.min, prevElem.output.minValue)
      }
    }

    val maxVal = if(prevElem.output.maxValue == prevElem.output.value) {
      if(forWorst.nonEmpty) {
        forWorst.max
      } else {
        mainRecord.value
      }
    } else {
      if(filteredLength <= 0) {
        prevElem.output.maxValue
      } else {
        math.max(mapped.max, prevElem.output.maxValue)
      }
    }
    val output = Output(
      timestamp = mainRecord.timestamp,
      value = mainRecord.value,
      numOfMeasurements = num,
      rollingSum = sum,
      minValue = minVal,
      maxValue = maxVal
    )

    ProcessedElement(output, Range(currPos - num + 1, currPos))
  }

  private def calculate(startPos: Int,
                        length: Int,
                        rollingWindow: Long,
                        data: Array[TimeRecord],
                        onResult: Output => Unit): Unit = {
    val endPos = startPos + length
    var currPos = endPos - 1
    var prevElem = calcFirstElement(endPos - 1, rollingWindow, data.view(startPos, endPos))
    val outBuilder = mutable.ArrayBuilder.make[ProcessedElement]()
    outBuilder += prevElem
    while (currPos > startPos) {
      prevElem = calcOtherElem(rollingWindow, data, prevElem)
      outBuilder += prevElem
      currPos -= 1
    }

    outBuilder.result().reverse.map(_.output).foreach(onResult)
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

  private def recordFilter(mainRecord: TimeRecord, rollingWindow: Long)(record: TimeRecord): Boolean = {
    record != null && mainRecord.timestamp - record.timestamp <= rollingWindow
  }
}
