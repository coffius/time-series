package io.koff.timeseries.mapped

import java.io.{File, RandomAccessFile}
import java.nio.MappedByteBuffer

/**
  * Reads records from a file using memory mapped files
  * @param file the file with data
  * @param pageSize size of a memory buffer (in bytes)
  */
class TimeRecordReader(val file: File, val pageSize: Long) {
  private val buffers: Array[MappedByteBuffer] = {
    val fileChannel = new RandomAccessFile(file, "r").getChannel
    val numOfPages = Math.ceil(fileChannel.size().toDouble / pageSize).toInt
    0 until numOfPages map { pageIndex =>
      val start = pageIndex * pageSize
      ???
    }
    ???
  }
}
/*
public class RandomLargeFileReader  {
    private static final long PAGE_SIZE = Integer.MAX_VALUE;
    private List<MappedByteBuffer> buffers = new ArrayList<MappedByteBuffer>();
    private final byte raw[] = new byte[1];

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/stu/test.txt");
        FileChannel fc = (new FileInputStream(file)).getChannel();
        StusMagicLargeFileReader buffer = new StusMagicLargeFileReader(fc);
        long position = file.length() / 2;
        String candidate = buffer.getString(position--);
        while (position >=0 && !candidate.equals('\n'))
            candidate = buffer.getString(position--);
        //have newline position or start of file...do other stuff
    }
    StusMagicLargeFileReader(FileChannel channel) throws IOException {
        long start = 0, length = 0;
        for (long index = 0; start + length < channel.size(); index++) {
            if ((channel.size() / PAGE_SIZE) == index)
                length = (channel.size() - index *  PAGE_SIZE) ;
            else
                length = PAGE_SIZE;
            start = index * PAGE_SIZE;
            buffers.add(index, channel.map(READ_ONLY, start, length));
        }
    }
    public String getString(long bytePosition) {
        int page  = (int) (bytePosition / PAGE_SIZE);
        int index = (int) (bytePosition % PAGE_SIZE);
        raw[0] = buffers.get(page).get(index);
        return new String(raw);
    }
}
 */