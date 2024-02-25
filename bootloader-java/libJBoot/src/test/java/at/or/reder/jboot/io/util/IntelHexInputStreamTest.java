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
import static com.google.common.truth.Truth.assertThat;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Wolfgang Reder
 */
public class IntelHexInputStreamTest
{

  @Test
  public void testRead() throws Exception
  {

    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("small.hex"));
    int read = is.read();
    long position = is.getPosition();
    assertThat(read).isEqualTo(0x11);
    assertThat(position).isEqualTo(0x1c00);
    assertThat(is.getCurrentMemorySpace()).isEqualTo(MemorySpace.FLASH);
  }

  @Test
  @SuppressWarnings("empty-statement")
  public void testFullRead() throws Exception
  {
    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("small.hex"));
    while (is.read() != -1);
    assertThat(is.getStartAddress().isPresent()).isTrue();
    assertThat(is.getStartAddress().getAsLong()).isEqualTo(0x1c00);
    assertThat(is.getPosition()).isEqualTo(0x01);
    assertThat(is.getCurrentMemorySpace()).isEqualTo(MemorySpace.FUSE);
  }

  @Test
  public void testScanSpacesHex() throws Exception
  {
    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("small.hex"));
    Map<MemorySpace, Long> scanned = is.scanSpaces();
    assertThat(scanned.size()).isEqualTo(2);
    assertThat(scanned.containsKey(MemorySpace.FLASH)).isTrue();
    assertThat(scanned.containsKey(MemorySpace.FUSE)).isTrue();
    assertThat(scanned.get(MemorySpace.FLASH)).isEqualTo(38L);
    assertThat(scanned.get(MemorySpace.FUSE)).isEqualTo(2L);
  }

  @Test
  public void testReadFuses() throws Exception
  {
    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("small.hex"),
                                                     MemorySpace.FUSE);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int read;
    while ((read = is.read()) != -1) {
      bos.write(read);
    }
    assertThat(bos.size()).isEqualTo(2);
    assertThat(bos.toByteArray()[0] & 0xff).isEqualTo(0xe0);
    assertThat(bos.toByteArray()[1] & 0xff).isEqualTo(0xda);
  }

  @Test
  public void testReadFlash() throws Exception
  {
    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("small.hex"),
                                                     MemorySpace.FLASH);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int read;
    while ((read = is.read()) != -1) {
      bos.write(read);
    }
    assertThat(bos.size()).isEqualTo(38);
  }

}
