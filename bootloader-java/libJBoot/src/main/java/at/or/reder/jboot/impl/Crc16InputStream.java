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

import at.or.reder.jboot.io.util.Crc16;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Wolfgang Reder
 */
@RequiredArgsConstructor
public final class Crc16InputStream extends InputStream
{

  private final InputStream wrapped;
  private final Crc16 crc = new Crc16();

  @Override
  public int read() throws IOException
  {
    int read = wrapped.read();
    if (read != -1) {
      crc.update((byte) read);
    }
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
    if (result != -1) {
      crc.update(b,
                 off,
                 result);
    }
    return result;
  }

  @Override
  public int read(byte[] b) throws IOException
  {
    return read(b,
                0,
                b.length);
  }

  public int getCrc()
  {
    return crc.getDigest();
  }

  @Override
  public void close() throws IOException
  {
    wrapped.close();
  }

}
