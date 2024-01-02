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
package at.or.reder.jboot;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Wolfgang Reder
 */
@Getter
@RequiredArgsConstructor
public enum BootloaderFeature
{
  FLASH_WRITE((short) 0x1001,
              MemorySpace.FLASH),
  FLASH_READ((short) 0x2002,
             MemorySpace.FLASH),
  FLASH_CHECK((short) 0x4004,
              MemorySpace.FLASH),
  EEPROM_WRITE((short) 0x1008,
               MemorySpace.EEPROM),
  EEPROM_READ((short) 0x2010,
              MemorySpace.EEPROM),
  EEPROM_CHECK((short) 0x4020,
               MemorySpace.EEPROM),
  FUSE_WRITE((short) 0x1040,
             MemorySpace.FUSE),
  FUSE_READ((short) 0x2080,
            MemorySpace.FUSE),
  SIGNATURE_READ((short) 0x2100,
                 MemorySpace.SIGNATURE),
  USER_ROW_WRITE((short) 0x1200,
                 MemorySpace.USER_ROW),
  USER_ROW_READ((short) 0x2200,
                MemorySpace.USER_ROW);

  private final short mask;
  private final MemorySpace memorySpace;

  public static Set<BootloaderFeature> getFromMask(int mask)
  {
    short tmpMask = (short) (mask & 0xffff);
    EnumSet<BootloaderFeature> result = EnumSet.noneOf(BootloaderFeature.class);
    for (BootloaderFeature feature : values()) {
      if ((tmpMask & feature.getMask()) == feature.getMask()) {
        result.add(feature);
      }
    }
    return result;
  }

  public static int createMask(@NonNull Collection<BootloaderFeature> features)
  {
    return features.stream()
            .mapToInt(BootloaderFeature::getMask)
            .reduce(0,
                    (a, b) -> a | b) & 0xffff;
  }

}
