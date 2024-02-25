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
package at.or.reder.jboot.impl.jboot;

import at.or.reder.jboot.Bootloader;
import at.or.reder.jboot.BootloaderFeature;
import at.or.reder.jboot.BootloaderListener;
import at.or.reder.jboot.DeviceSignature;
import at.or.reder.jboot.JBootNative;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.impl.SerialBootloader;
import at.or.reder.jboot.impl.jboot.Record;
import at.or.reder.jboot.io.util.Crc16;
import gnu.io.PortFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Wolfgang Reder
 */
@Log(topic = "at.or.reder.jboot.JBootBootloader")
final class JBootBootloader extends SerialBootloader
{

  private final JBootNative nativeApi = new JBootNative()
  {
    @Override
    public boolean nop() throws IOException
    {
      return JBootBootloader.this.nop() == Response.RSP_OK_BOOTLOADER;
    }

    @Override
    public OptionalInt getCrc16() throws IOException
    {
      Record response = sendReceive(Record.commandNop());
      if (response.getResponse() == Response.RSP_OK_BOOTLOADER) {
        return OptionalInt.of(response.getWordValue() & 0xffff);
      } else {
        return OptionalInt.empty();
      }
    }

    @Override
    public boolean checkFirmware(int crc16) throws IOException
    {
      return sendReceive(Record.commandCheckProgram(crc16)).getResponse() == Response.RSP_OK_BOOTLOADER;
    }

    @Override
    public Optional<DeviceSignature> readSignature() throws IOException
    {
      return JBootBootloader.this.readSignature();
    }

  };
  private Record rebootRecord = Record.commandReboot();

  public JBootBootloader(Map<String, String> settings,
                         PortFactory portFactory)
  {
    super(settings,
          portFactory);
  }

  @Override
  public Set<BootloaderFeature> getFeatures()
  {
    return Set.of(BootloaderFeature.FLASH_WRITE,
                  BootloaderFeature.SIGNATURE_READ);
  }

  private int readShort(InputStream streamIn,
                        Crc16 crc) throws IOException
  {
    byte[] buffer = new byte[2];
    int read = streamIn.read(buffer);
    if (read == -1) {
      return -1;
    }
    if (read == 1) {
      buffer[1] = (byte) 0xff;
    }
    crc.update(buffer);
    return (buffer[0] & 0xff) + ((buffer[1] & 0xff) << 8);
  }

  private static final int NUMBYTES = 0xffff;

  @Override
  public boolean upload(@NonNull MemorySpace memorySpace,
                        @NonNull InputStream streamIn,
                        BootloaderListener listener) throws IOException, UnsupportedOperationException
  {
    if (listener == null) {
      listener = (Bootloader bootloader, MemorySpace space, long bytesWritten, long bytesTotal) -> {
      };
    }
    if (memorySpace == MemorySpace.FLASH) {
      boolean result = false;
      Record response = sendReceive(Record.commandEnterBootloader());
      if (response.getResponse() == Response.RSP_OK_BOOTLOADER) {
        long bytesToSend = response.getWordValue() & 0xffff;
        long bytesSent = 0;
        Record toSend = Record.commandProgram(-1);
        Crc16 crc = new Crc16();
        int read;
        do {
          read = readShort(streamIn,
                           crc);
          if (read != -1) {
            response = sendReceive(toSend.setWordValue((short) read));
            if (response.getResponse() == Response.RSP_OK_BOOTLOADER) {
              bytesSent = response.getWordValue() & 0xffff;
            }
          }
          listener.onTransfer(this,
                              memorySpace,
                              bytesSent,
                              bytesToSend);
        } while (read != -1 && response.getResponse() == Response.RSP_OK_BOOTLOADER && bytesSent < NUMBYTES);
        toSend.setWordValue(0xffff);
        while (response.getResponse() == Response.RSP_OK_BOOTLOADER && bytesSent < NUMBYTES) {
          crc.update((byte) 0xff);
          crc.update((byte) 0xff);
          response = sendReceive(toSend);
          if (response.getResponse() == Response.RSP_OK_BOOTLOADER) {
            bytesSent = response.getWordValue() & 0xffff;
            listener.onTransfer(this,
                                memorySpace,
                                bytesSent,
                                bytesToSend);
          }
        }
        //result = sendReceive(Record.commandCheckProgram(crc.getDigest())).getResponse() == Response.RSP_OK_BOOTLOADER;
        //if (result) {
        rebootRecord = Record.commandReboot(crc.getDigest());
        // }
      }
      return result;
    } else {
      throw new UnsupportedOperationException("Memoryspace " + memorySpace.name() + " not supported");
    }
  }

  @Override
  public boolean isConnected()
  {
    if (super.isConnected()) {
      try {
        return nop() == Response.RSP_OK_BOOTLOADER;
      } catch (IOException ex) {
        log.log(Level.SEVERE,
                "isConnected",
                ex);
      }
    }
    return false;
  }

  @Override
  public Optional<DeviceSignature> readSignature() throws IOException, UnsupportedOperationException
  {
    if (enterBootloader() == Response.RSP_OK_BOOTLOADER) {
      Record response = sendReceive(Record.commandReadSignature());
      if (response.getResponse() == Response.RSP_OK_BOOTLOADER) {
        return Optional.of(new DeviceSignature(response.getData()));
      }
    }
    return Optional.empty();
  }

  private Response nop() throws IOException
  {
    Record response = sendReceive(Record.commandNop());
    return response.getResponse();
  }

  private Response enterBootloader() throws IOException
  {
    Record response = sendReceive(Record.commandEnterBootloader());
    return response.getResponse();
  }

  private Record sendReceive(Record toSend) throws IOException
  {
    if (!super.isConnected()) {
      throw new IOException("Not connected");
    }
    Record response = new Record();
    toSend.getBuffer().mark();
    try {
      out.write(toSend.getBuffer());
    } finally {
      toSend.getBuffer().reset();
    }
    in.read(response.getBuffer());
    return response;
  }

  @Override
  public Lookup getLookup()
  {
    return Lookups.singleton(nativeApi);
  }

  @Override
  public void close() throws IOException
  {
    sendReceive(rebootRecord);
    super.close();
  }

}
