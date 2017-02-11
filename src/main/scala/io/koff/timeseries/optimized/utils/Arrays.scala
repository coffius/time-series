package io.koff.timeseries.optimized.utils

import scala.annotation.tailrec

/**
  * Created by coffius on 11.02.2017.
  */
object Arrays {
  def traverse[T](array: Array[T], start: Int, length: Int, step: Int = 1)
                 (whileCond: T => Boolean)(operation: T => Unit): Int = {
    require(step != 0, s"step[$step] should be greater or less then 0")

    val realLength = if(step > 0) {
      math.min(length, array.length - start)
    } else {
      math.min(start + 1, length)
    }

    val steps = realLength / Math.abs(step)
    _traverse(array, start, steps, step, 0)(whileCond)(operation)
  }

  @tailrec
  private def _traverse[T](array: Array[T],
                           currPos: Int,
                           remainSteps: Int,
                           step: Int,
                           processed: Int)
                          (whileCond: T => Boolean)
                          (operation: T => Unit): Int = {
    val elem = array(currPos)
    if(remainSteps > 0 && whileCond(elem)) {
      operation(elem)
      _traverse(array, currPos + step, remainSteps - 1, step, processed + 1)(whileCond)(operation)
    } else {
      processed
    }
  }

  def foldLeft[T](a)
}
