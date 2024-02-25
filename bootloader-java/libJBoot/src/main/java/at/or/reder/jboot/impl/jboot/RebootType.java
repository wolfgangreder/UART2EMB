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
package at.or.reder.jboot.impl.jboot;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Wolfgang Reder
 */
@Getter
@RequiredArgsConstructor
public enum RebootType
{

  REBOOT_NORMAL((byte) 0x00),
  REBOOT_CRC16((byte) 0x01);
  private final byte magic;

  public static Optional<RebootType> valueOfMagic(int magic)
  {
    for (RebootType r : values()) {
      if (r.getMagic() == (byte) (magic & 0xff)) {
        return Optional.of(r);
      }
    }
    return Optional.empty();
  }

}
