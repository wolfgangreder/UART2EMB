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
package at.or.reder.jboot;

import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 *
 * @author Wolfgang Reder
 */
@EqualsAndHashCode
public final class DeviceSignature
{

  private final byte[] signatureBytes;
  @EqualsAndHashCode.Exclude
  private String string;

  public DeviceSignature(@NonNull ByteBuffer buffer)
  {
    signatureBytes = new byte[buffer.remaining()];
    for (int i = 0; i < buffer.remaining(); ++i) {
      signatureBytes[i] = buffer.get();
    }
  }

  @Override
  public synchronized String toString()
  {
    if (string == null) {
      string = "DeviceSignature " + BaseEncoding.base16().encode(signatureBytes);
    }
    return string;
  }

}
