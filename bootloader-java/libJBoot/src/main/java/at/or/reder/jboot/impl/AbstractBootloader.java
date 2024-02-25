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

import at.or.reder.jboot.Bootloader;
import at.or.reder.jboot.BootloaderFeature;
import at.or.reder.jboot.BootloaderListener;
import at.or.reder.jboot.MemorySpace;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Set;

/**
 *
 * @author Wolfgang Reder
 */
public abstract class AbstractBootloader implements Bootloader
{

  protected ReadableByteChannel in;
  protected WritableByteChannel out;

  @Override
  public Set<BootloaderFeature> getFeatures()
  {
    return Set.of(BootloaderFeature.FLASH_WRITE);
  }

  @Override
  public boolean download(MemorySpace memorySpace,
                          OutputStream out,
                          BootloaderListener listener) throws IOException, UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
