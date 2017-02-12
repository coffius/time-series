package io.koff.timeseries.optimized

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.Source

/**
  * Attempt to make some optimizations
  */
object OptimizedCalculator {
  // One of the ideas here is to use SeqView to avoid unnecessary copying
  type DataView = mutable.IndexedSeqView[TimeRecord, Array[TimeRecord]]

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
    var copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)

    while (copied > 0) {
      calculate(bufferSize, copied, rollingWindow, buffer, onResult)
      Array.copy(buffer, bufferSize, buffer, 0, bufferSize)
      copied = copyToArray(recordIter, buffer, bufferSize, bufferSize)
    }
  }

  /**
    * Makes calculation for the first element.<br/>
    * For the first element we have to traverse through all the elements in rolling window.
    */
  private def calcFirstElement(currPos: Int, rollingWindow: Long, data: DataView): ProcessedElement = {
    val mainRecord  = data.last
    // we don't want to traverse all elements in the input array, so we use `takeWhile(...)`
    // in order to stop as soon as we reach the end of the rolling window or the end in the input array
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

  /**
    * Makes calculation for each element except the first one.<br/>
    * It is unnecessary to make all calculations for elem(N-1) because some info has been already calculated for elem(N)
    */
  private def calcOtherElem(rollingWindow: Long,
                            data: Array[TimeRecord],
                            prevElem: ProcessedElement): ProcessedElement = {
    val currPos = prevElem.range.right - 1
    val mainRecord = data(currPos)
    // the description of the left range in the pic here: docs/optimization.jpg
    val leftRange = data.view(0, prevElem.range.left).reverse
    val withFilters = leftRange.takeWhile(recordFilter(mainRecord, rollingWindow))
    val mapped = withFilters.map(_.value)
    val filteredLength = withFilters.length
    // reuse values of numOfMeasurements and sum in order to lower number of calculations
    val num = filteredLength + prevElem.output.numOfMeasurements - 1
    val sum = mapped.sum + prevElem.output.rollingSum - prevElem.output.value

    val forWorst = data.view(0, currPos + 1).reverse.takeWhile(recordFilter(mainRecord, rollingWindow)).map(_.value)
    // in case with min/max values the situation is a bit different
    // if the value of the previous element is min val then we need to scal the whole rolling window in order to find
    // the current min value
    // but if it is not then we need to scan only the left range and then compare the result with prevElem.output.minValue
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

    // the same is with max values
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

  /**
    * Calculates output for each record in input array
    * @param startPos the start position in input array
    * @param length count of elements to process
    * @param rollingWindow length of a rolling window
    * @param data the input array
    * @param onResult the function which is executed for every output
    */
  private def calculate(startPos: Int,
                        length: Int,
                        rollingWindow: Long,
                        data: Array[TimeRecord],
                        onResult: Output => Unit): Unit = {
    val endPos = startPos + length
    val firstElement = calcFirstElement(endPos - 1, rollingWindow, data.view(startPos, endPos))
    val outBuilder = mutable.ArrayBuilder.make[ProcessedElement]()
    outBuilder += firstElement

    // tailrec func to avoid mutable counters
    @tailrec
    def _calc(currPos: Int, prevElem: ProcessedElement): Unit = {
      if(currPos > startPos) {
        val elem = calcOtherElem(rollingWindow, data, prevElem)
        outBuilder += elem
        _calc(currPos - 1, elem)
      }
    }

    _calc(endPos - 1, firstElement)

    outBuilder.result().reverse.map(_.output).foreach(onResult)
  }

  /**
    * Converts a string in TimeRecord if it is possible and throws IllegalStateException if it is not
    * @param str the string to convert
    * @return record
    * @throws IllegalStateException if the string has a wrong format
    */
  private def toTimeRecord(str: String) = str match {
    case TimeRecord(record) => record
    case invalidStr => throw new IllegalStateException(s"invalid format of string: $invalidStr")
  }

  /**
    * Copies values from a iterator to array and returns number of copied elements
    */
  def copyToArray[T](src: Iterator[T], dst: Array[T], start: Int, len: Int)(): Int = {
    var i = start
    val end = start + len
    while (i < end && src.hasNext) {
      dst(i) = src.next()
      i += 1
    }
    i - start
  }

  /**
    * Filters records according rolling window
    */
  private def recordFilter(mainRecord: TimeRecord, rollingWindow: Long)(record: TimeRecord): Boolean = {
    record != null && mainRecord.timestamp - record.timestamp <= rollingWindow
  }
}
