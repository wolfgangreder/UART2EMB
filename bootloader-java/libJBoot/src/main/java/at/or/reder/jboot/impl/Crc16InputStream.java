/*
 * Copyright 2023 Wolfgang Reder.
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
package at.or.reder.jboot.impl;

import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Wolfgang Reder
 */
@Getter
@RequiredArgsConstructor
public final class Crc16InputStream extends InputStream
{

  private final InputStream wrapped;
  private int crc = 0;

  @Override
  public int read() throws IOException
  {
    int read = wrapped.read();
    crc = crc16Update(crc,
                      read);
    return read;
  }

  @Override
  public int read(byte[] b,
                  int off,
                  int len) throws IOException
  {
    int result = wrapped.read(b,
                              off,
                              len);
    crc16Update(b,
                off,
                result);
    return result;
  }

  @Override
  public int read(byte[] b) throws IOException
  {
    return read(b,
                0,
                b.length);
  }

  private void crc16Update(byte[] b,
                           int off,
                           int len)
  {
    for (int i = 0; i < len; ++i) {
      crc = crc16Update(b[i + off],
                        crc);
    }
  }

  private int crc16Update(int crc,
                          int a)
  {
    int i;

    crc ^= a;
    for (i = 0; i < 8; ++i) {
      if ((crc & 1) != 0) {
        crc = (crc >> 1) ^ 0xA001;
      } else {
        crc = (crc >> 1);
      }
    }

    return crc;
  }

  @Override
  public void close() throws IOException
  {
    wrapped.close();
  }

}
