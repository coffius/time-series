package io.koff.timeseries.naive

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

  /** Length of a string for timestamps */
  val timestampLen = 10
  /** Length of a string for values */
  val valueLen = 7
  /** Length of a delimiter between a timestamp and a value */
  val delimiterLen = 1
  /** Number of characters for at the end of a line */
  val eolLen = 2 // /n/r
}
