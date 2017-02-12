package io.koff.timeseries.common

/**
  * Input data
  * @param timestamp a timestamp of a measurement in seconds
  * @param value the measurement of price ratio
  */
case class TimeRecord(timestamp: Long, value: Double)

object TimeRecord {
  /**
    * Converts a string to TimeRecord. String should be like: `"1355270609 1.80215"`
    */
  def unapply(str: String): Option[TimeRecord] = {
    val splitChar = if(str.contains(" ")) { " " } else { "\t" }
    val strings = str.split(splitChar)
    if(strings.size == 2) {
      val record = TimeRecord(
        timestamp = strings(0).toLong,
        value = strings(1).toDouble
      )

      Some(record)
    } else {
      None
    }
  }
}

