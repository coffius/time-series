package io.koff.timeseries.common

case class TimeRecord(timestamp: Long, value: Double)

object TimeRecord {
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

