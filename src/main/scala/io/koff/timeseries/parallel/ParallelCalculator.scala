package io.koff.timeseries.parallel

import java.io.File

import io.koff.timeseries.common.{Output, TimeRecord}

import scala.annotation.tailrec
import scala.collection.{SeqView, mutable}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Attempt to make some optimizations
  */
object ParallelCalculator {
  // One of the ideas here is to use SeqView to avoid unnecessary copying
  type DataView = SeqView[TimeRecord, Vector[TimeRecord]]

  private val Parallelization = 2

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

    val threadBuffer = math.ceil(bufferSize.toDouble / Parallelization).toInt

    def loadAndLaunch(prevBuffer: Vector[TimeRecord], prevReadF: Future[_], prevOnResultF: Future[_]): Future[_] = {
      for {
        _ <- prevReadF
        readF = Future { recordIter.take(bufferSize).toVector }
        buffer <- readF
        _ <- if(buffer.nonEmpty) {
          val futures = launchInParallel(prevBuffer ++ buffer, threadBuffer, rollingWindow, prevBuffer.length, buffer.length, Seq.empty)
          val resultF = Future.sequence(futures)
          for {
            _ <- prevOnResultF
            result <- resultF
            flatten = result.flatten
            onResultF = Future {
              flatten.map(_.output).foreach(onResult)
            }
            _ <- loadAndLaunch(buffer, readF, onResultF)
          } yield {
            ()
          }
        } else {
          Future.successful(())
        }
      } yield {
        ()
      }
    }

    val mainFuture = loadAndLaunch(Vector.empty, Future.successful(()), Future.successful(()))
    Await.result(mainFuture, Duration.Inf)
  }

  @tailrec
  private def launchInParallel(buffer: Vector[TimeRecord],
                               threadBuffer: Int,
                               rollingWindow: Long,
                               pos: Int,
                               remain: Int,
                               futures: Seq[Future[Array[ProcessedElement]]]): Seq[Future[Array[ProcessedElement]]] = {
    if(remain <= 0) {
      futures
    } else {
      val length = math.min(threadBuffer, math.min(remain, buffer.size - pos))
      val future = Future {
        calculate(pos, length, rollingWindow, buffer)
      }
      launchInParallel(buffer, threadBuffer, rollingWindow, pos + length, remain - length, futures :+ future)
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
                            data: Vector[TimeRecord],
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
    */
  private def calculate(startPos: Int,
                        length: Int,
                        rollingWindow: Long,
                        buffer: Vector[TimeRecord]): Array[ProcessedElement] = {
    val endPos = startPos + length
    val firstElement = calcFirstElement(endPos - 1, rollingWindow, buffer.view(0, endPos))
//    println(s"pos: $startPos, length: $length, firstElem: $firstElement")
    val outBuilder = mutable.ArrayBuilder.make[ProcessedElement]()
    outBuilder += firstElement

    // tailrec func to avoid mutable counters
    @tailrec
    def _calc(currPos: Int, prevElem: ProcessedElement): Unit = {
      if(currPos > startPos) {
        val elem = calcOtherElem(rollingWindow, buffer, prevElem)
        outBuilder += elem
        _calc(currPos - 1, elem)
      }
    }

    _calc(endPos - 1, firstElement)

    outBuilder.result().reverse
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
