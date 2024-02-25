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

import at.or.reder.jboot.Bootloader;
import at.or.reder.jboot.BootloaderFactory;
import at.or.reder.jboot.BootloaderStub;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.io.util.IntelHexInputStream;
import static com.google.common.truth.Truth.assertThat;
import gnu.io.PortFactory;
import gnu.io.SerialPort;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Wolfgang Reder
 */
@ExtendWith(MockitoExtension.class)
public class JBootBootloaderIT
{

  @Mock
  private static SerialPort mockPort;
  private final PortFactory portFactory = (String portIdentifier) -> mockPort;

  private final Map<String, String> connectionProps = Map.of(BootloaderFactory.PROP_CONNECTION,
                                                             "/dev/ttyS666");
  private BootloaderStub bootloaderStub;

  @BeforeEach
  public void init() throws Exception
  {
    bootloaderStub = BootloaderStub.aTmega8(0x1c00);
    lenient().when(mockPort.getInputStream()).thenReturn(bootloaderStub.getInput());
    lenient().when(mockPort.getOutputStream()).thenReturn(bootloaderStub.getOutput());
  }

  @Test
  public void testCreate() throws IOException
  {
    Optional<BootloaderFactory> factory = BootloaderFactory.findFactory(BootloaderFactory.ID_JBOOT);
    assertThat(factory.isPresent()).isTrue();
    assertThat(factory.get()).isInstanceOf(JBootBootloaderFactory.class);
    Bootloader bootloader = factory.get().createBootloader(connectionProps);
    assertThat(bootloader).isInstanceOf(JBootBootloader.class);
  }

  @Test
  public void testUpload() throws Exception
  {
    JBootBootloader loader = new JBootBootloader(connectionProps,
                                                 portFactory);
    IntelHexInputStream is = new IntelHexInputStream(getClass().getResourceAsStream("/at/or/reder/jboot/io/util/small.hex"),
                                                     MemorySpace.FLASH);
    loader.connect();
    assertThat(loader.upload(MemorySpace.FLASH,
                             is,
                             null)).isTrue();
    loader.close();
    assertThat(bootloaderStub.isEndCommandLoop()).isTrue();
    assertThat(loader.isConnected()).isFalse();
  }

  @Test
  public void testIsConnected() throws Exception
  {
    JBootBootloader loader = new JBootBootloader(connectionProps,
                                                 portFactory);
    loader.connect();
    assertThat(loader.isConnected()).isTrue();
    loader.close();
    assertThat(loader.isConnected()).isFalse();
  }

}
