package io.koff.timeseries.optimized.utils

import org.scalatest.{FreeSpec, Matchers}

class ArraysSpec extends FreeSpec with Matchers {
  "should traverse array" in {
    val array = 1 to 10 toArray
    var counter = 0
    val processed = Arrays.traverse(array, 4, 7) {_ <= 9} {_ => counter += 1}
    counter shouldBe 5
    processed shouldBe 5
  }

  "should reverse array" in {
    val array = 1 to 10 toArray
    var counter = 0
    val processed = Arrays.traverse(array, start = 6, length = 7, step = -1) {_ >= 2} {_ => counter += 1}
    counter shouldBe 6
    processed shouldBe 6
  }
}
