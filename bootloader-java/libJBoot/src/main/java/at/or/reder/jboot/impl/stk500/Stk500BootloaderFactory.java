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

import at.or.reder.jboot.Bootloader;
import at.or.reder.jboot.BootloaderFactory;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Wolfgang Reder
 */
@ServiceProvider(service = BootloaderFactory.class)
public class Stk500BootloaderFactory implements BootloaderFactory
{

  @Override
  public String getName()
  {
    return getClass().getSimpleName();
  }

  @Override
  public UUID getId()
  {
    return UUID.fromString("2fef8a55-e6f8-448f-a04b-3f45ab0f8e36");
  }

  @Override
  public Bootloader createBootloader(Map<String, String> properties) throws IOException
  {
    return new Stk500Bootloader(properties);
  }

}
