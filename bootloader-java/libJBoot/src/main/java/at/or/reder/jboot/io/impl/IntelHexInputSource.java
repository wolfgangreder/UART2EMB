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
package at.or.reder.jboot.io.impl;

import at.or.reder.jboot.DeviceSignature;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.io.Source;
import at.or.reder.jboot.io.util.IntelHexInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;

/**
 *
 * @author Wolfgang Reder
 */
public class IntelHexInputSource implements Source
{

  private Map<MemorySpace, Long> supportedSpaces;
  private final URI uri;

  public IntelHexInputSource(@NonNull URI uri)
  {
    this.uri = uri;
  }

  @Override
  public Set<MemorySpace> getSupportedSpaces() throws IOException
  {
    checkSupportedSpaces();
    return Collections.unmodifiableSet(supportedSpaces.keySet());
  }

  private void checkSupportedSpaces() throws IOException
  {
    if (supportedSpaces == null) {
      try (IntelHexInputStream is = new IntelHexInputStream(uri.toURL().openStream())) {
        supportedSpaces = Collections.unmodifiableMap(is.scanSpaces());
      }
    }
  }

  @Override
  public Optional<DeviceSignature> getDeviceSignature()
  {
    return Optional.empty();
  }

  @Override
  public long getSize(@NonNull MemorySpace space) throws IOException
  {
    checkSupportedSpaces();
    return supportedSpaces.getOrDefault(space,
                                        0L);
  }

  @Override
  public InputStream openStream(@NonNull MemorySpace space) throws IOException
  {
    return new IntelHexInputStream(uri.toURL().openStream(),
                                   space);
  }

}
