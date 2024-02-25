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

import static com.google.common.truth.Truth.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Wolfgang Reder
 */
public class Crc16Test
{

  public Crc16Test()
  {
  }

  @Test
  public void smallTest()
  {
    Crc16 crc16 = new Crc16(0xcafe);
    crc16.update((byte) 0xaf);
    crc16.update((byte) 0xff);
    crc16.update((byte) 0);
    int result = crc16.getDigest();
    assertThat(result).isEqualTo(0x8146);
  }

}
