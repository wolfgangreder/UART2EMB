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

/**
 *
 * @author Wolfgang Reder
 */
public final class Crc16
{

  private int crc;

  public Crc16()
  {
    this(0);
  }

  public Crc16(int init)
  {
    crc = init;
  }

  public void reset()
  {
    crc = 0;
  }

  public int getDigest()
  {
    return crc & 0xffff;
  }

  public void update(byte b)
  {
    int i;

    crc ^= (b & 0xff);
    for (i = 0; i < 8; ++i) {
      if ((crc & 1) != 0) {
        crc = ((crc >> 1) ^ 0xA001) & 0xffff;
      } else {
        crc = (crc >> 1);
      }
    }
  }

  public void update(byte[] data)
  {
    update(data,
           0,
           data.length);
  }

  public void update(byte[] data,
                     int offset,
                     int len)
  {
    for (int i = 0; i < len; ++i) {
      update(data[i + offset]);
    }
  }

}
