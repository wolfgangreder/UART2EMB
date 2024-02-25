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

import at.or.reder.jboot.BootloaderListener;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.impl.SerialBootloader;
import gnu.io.PortFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author Wolfgang Reder
 */
public class Stk500Bootloader extends SerialBootloader
{

  public Stk500Bootloader(Map<String, String> properties,
                          PortFactory portFactory)
  {
    super(properties,
          portFactory);
  }

  @Override
  public boolean upload(MemorySpace memorySpace,
                        InputStream is,
                        BootloaderListener listener) throws IOException, UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }

}
