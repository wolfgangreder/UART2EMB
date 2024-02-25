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

import gnu.io.PortFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.openide.util.Lookup;

/**
 *
 * @author Wolfgang Reder
 */
public interface BootloaderFactory
{

  public static UUID ID_JBOOT = UUID.fromString("8d6672f5-6f95-4edb-a9b3-d65404529e2c");

  public static final String PROP_CONNECTION = "at.or.reder.jboot.connection";
  public static final String PROP_SPEED = "at.or.reder.jboot.speed";

  public String getName();

  public UUID getId();

  public Bootloader createBootloader(Map<String, String> properties,
                                     PortFactory portFactory) throws IOException;

  public default Bootloader createBootloader(Map<String, String> properties) throws IOException
  {
    return createBootloader(properties,
                            null);
  }

  public static Optional<BootloaderFactory> findFactory(UUID id)
  {
    return (Optional<BootloaderFactory>) Lookup.getDefault()
            .lookupAll(BootloaderFactory.class)
            .stream()
            .filter(f -> f.getId().equals(id))
            .findFirst();
  }

}
