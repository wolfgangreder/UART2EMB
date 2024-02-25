/*
 * Copyright 2024 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.jboot.io.util;

import at.or.reder.jboot.MemorySpace;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 *
 * @author Wolfgang Reder
 */
public class IntelHexInputStream extends PositionedInputStream
{

  private OptionalLong startAddress = OptionalLong.empty();
  private int recordType;
  private long position = -1;
  private long offset = 0;
  private long segmentOffset = 0;
  private LineNumberReader reader;
  private final Queue<Integer> queue = new LinkedList<>();
  private final MemorySpace filter;

  public IntelHexInputStream(InputStream strm,
                             MemorySpace filter)
  {
    this(new InputStreamReader(strm),
         filter);
  }

  public IntelHexInputStream(InputStream strm)
  {
    this(new InputStreamReader(strm));
  }

  public IntelHexInputStream(Reader reader)
  {
    this(reader,
         null);
  }

  public IntelHexInputStream(Reader reader,
                             MemorySpace filter)
  {
    this.reader = new LineNumberReader(reader);
    this.filter = filter;
  }

  @Override
  public long getPosition()
  {
    return position + offset;
  }

  @Override
  public int read() throws IOException
  {
    Integer result = queue.poll();
    while ((result == null) && (readNextLine())) {
      result = queue.poll();
    }
    if (result != null) {
      ++position;
      return result & 0xff;
    }
    return -1;
  }

  private boolean readNextLine() throws IOException
  {
    String line;
    while ((line = reader.readLine()) != null) {
      parseLine(line);
      if (filter == null || filter == getCurrentMemorySpace()) {
        return true;
      } else {
        queue.clear();
      }
    }
    return false;
  }

  private void parseLine(String pLine) throws IOException
  {
    try {
      checkLine(pLine);
      int byteCount = Integer.parseInt(pLine.substring(1,
                                                       3),
                                       16);
      recordType = Integer.parseInt(pLine.substring(7,
                                                    9),
                                    16);
      switch (recordType) {
        case 0x00:// DataRecord
          position = Integer.parseInt(pLine.substring(3,
                                                      7),
                                      16) - 1;
          processDataRecord(pLine.substring(9),
                            byteCount);
          break;
        case 0x01://EOF
          break;
        case 0x02://extendedsegementrecord
          processExtendedSegmentRecord(pLine.substring(9));
          break;
        case 0x03://segementrecord
          processSegmentRecord(pLine.substring(9));
          break;
        case 0x04://extendendlinearaddress
          processLinearSegmentRecord(pLine.substring(9));
          break;
        case 0x05://startlinearaddress
          throw new IOException("Unexpected recordtype");
        default:
          throw new IOException("Unexpected recordtype");
      }
    } catch (NumberFormatException e) {
      throw new IOException("Invalid HexDigit at line " + reader.getLineNumber());
    }
  }

  private void processSegmentRecord(String pLine)
  {
    startAddress = OptionalLong.of(Long.parseLong(pLine.substring(0,
                                                                  8),
                                                  16));
  }

  private void processExtendedSegmentRecord(String pLine)
  {
    int lo = Integer.parseInt(pLine.substring(0,
                                              2),
                              16);
    int high = Integer.parseInt(pLine.substring(2,
                                                4),
                                16);
    offset = (lo | high << 8) << 4;
  }

  private void processLinearSegmentRecord(String pLine)
  {
    int value = Integer.parseInt(pLine.substring(0,
                                                 4),
                                 16);
    position = 0;
    segmentOffset = value << 16;
  }

  private void processDataRecord(String pLine,
                                 int byteCount)
  {
    for (int i = 0; i < 2 * byteCount; i += 2) {
      queue.offer(Integer.valueOf(pLine.substring(i,
                                                  i + 2),
                                  16));
    }
  }

  private boolean checkLine(String pLine) throws IOException
  {
    if (!pLine.startsWith(":")) {
      throw new IOException("Invalid record at line " + reader.getLineNumber());
    }
    if (pLine.length() % 2 == 0) {
      throw new IOException("Invalid line length at line " + reader.getLineNumber());
    }
    int sum = 0;
    for (int i = 1; i < pLine.length(); i += 2) {
      sum += Integer.parseInt(pLine.substring(i,
                                              i + 2),
                              16);
    }
    if ((sum & 0xff) != 0) {
      throw new IOException("Invalid checksum at line " + reader.getLineNumber());
    }
    return true;
  }

  public OptionalLong getStartAddress()
  {
    return startAddress;
  }

  public long getSegmentOffset()
  {
    return segmentOffset;
  }

  public MemorySpace getCurrentMemorySpace()
  {
    int selector = (int) (segmentOffset >> 16) & 0xffff;
    return switch (selector) {
      case 0x00 ->
        MemorySpace.FLASH;
      case 0x81 ->
        MemorySpace.EEPROM;
      case 0x82 ->
        MemorySpace.FUSE;
      default ->
        null;
    };
  }

  public Map<MemorySpace, Long> scanSpaces() throws IOException
  {
    Map<MemorySpace, AtomicLong> result = new HashMap<>();
    while (read() != -1) {
      result.computeIfAbsent(getCurrentMemorySpace(),
                             s -> new AtomicLong())
              .incrementAndGet();
    }
    return result.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                                      l -> l.getValue().get()));
  }

  @Override
  public void close() throws IOException
  {
    reader.close();
  }

}
