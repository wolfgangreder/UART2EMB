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
package at.or.reder.jboot.impl.stk500;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Wolfgang Reder
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCodes
{
  STK_OK((byte) 0x13),
  STK_ERROR((byte) 0xff);

  private final byte magic;

  public static Optional<ResponseCodes> valueOfMagic(int magic)
  {
    for (ResponseCodes r : values()) {
      if (r.getMagic() == (magic & 0xff)) {
        return Optional.of(r);
      }
    }
    return Optional.empty();
  }

}
