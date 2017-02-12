package io.koff.timeseries.generators

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}

/**
  * Generator for worst case scenario when all the data is in one rolling window
  */
object Generator {
  private val NumberOfRecords = 100000000
  private val filename = "data/big_data.txt"
  def main(args: Array[String]) {
    val file = new File(filename)
    if(file.exists()) {
      if(file.delete()) {
        if(!file.createNewFile()) {
          throw new IllegalStateException(s"Cannot create the file: $filename")
        }
      } else {
        throw new IllegalStateException(s"Cannot remove the file: $filename")
      }
    } else {
      if(!file.createNewFile()) {
        throw new IllegalStateException(s"Cannot create the file: $filename")
      }
    }

    val out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))
    for {
      _ <- 0 until NumberOfRecords
    } yield {
      out.println("1355270609 1.80215d")
    }
    out.close()
    println("test data have been generated")
  }
}
