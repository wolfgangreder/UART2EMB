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

import at.or.reder.jboot.BootloaderFeature;
import at.or.reder.jboot.JBootNative;
import at.or.reder.jboot.MemorySpace;
import at.or.reder.jboot.impl.Crc16InputStream;
import at.or.reder.jboot.impl.SerialBootloader;
import at.or.reder.jboot.impl.jboot.Record;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
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
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean checkFirmware(int crc16) throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ByteBuffer readSignature() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

  };

  public JBootBootloader(Map<String, String> settings)
  {
    super(settings);
  }

  @Override
  public Set<BootloaderFeature> getFeatures()
  {
    return Set.of(BootloaderFeature.FLASH_WRITE,
                  BootloaderFeature.SIGNATURE_READ);
  }

  @Override
  public boolean upload(@NonNull MemorySpace memorySpace,
                        @NonNull InputStream streamIn) throws IOException, UnsupportedOperationException
  {
    if (memorySpace == MemorySpace.FLASH) {
      boolean result = false;
      if (enterBootloader() == Response.RSP_OK) {
        Response resp = Response.RSP_OK;
        Record toSend = Record.commandProgram(-1);
        try (Crc16InputStream is = new Crc16InputStream(streamIn)) {
          while (resp == Response.RSP_OK) {
            resp = sendReceive(toSend.setByteValue(is.read())).getResponse();
          }
          result = sendReceive(Record.commandCheckProgram(is.getCrc())).getResponse() == Response.RSP_OK;
        }
        sendReceive(Record.commandReboot());
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
        return nop() == Response.RSP_OK;
      } catch (IOException ex) {
        log.log(Level.SEVERE,
                "isConnected",
                ex);
      }
    }
    return false;
  }

  @Override
  public Optional<ByteBuffer> readSignature() throws IOException, UnsupportedOperationException
  {
    if (enterBootloader() == Response.RSP_OK) {
      Record response = sendReceive(Record.commandReadSignature());
      if (response.getResponse() == Response.RSP_OK) {
        return Optional.of(response.getData());
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
    return sendReceive(Record.commandEnterBootloader()).getResponse();
  }

  private Record sendReceive(Record toSend) throws IOException
  {
    if (!super.isConnected()) {
      throw new IOException("Not connected");
    }
    Record response = new Record();
    out.write(toSend.getBuffer());
    in.read(response.getBuffer());
    return response;
  }

  @Override
  public Lookup getLookup()
  {
    return Lookups.singleton(nativeApi);
  }

}
