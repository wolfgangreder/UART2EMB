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

import at.or.reder.jboot.BootloaderFactory;
import gnu.io.PortFactory;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Map;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.openide.util.Lookup;

/**
 *
 * @author Wolfgang Reder
 */
@Log(topic = "at.or.reder.jboot")
public abstract class SerialBootloader extends AbstractBootloader
{

  @NonNull
  private final Map<String, String> properties;
  private final PortFactory portFactory;
  private SerialPort port;

  protected SerialBootloader(Map<String, String> properties,
                             PortFactory portFactory)
  {
    this.properties = properties;
    this.portFactory = portFactory != null ? portFactory : Lookup.getDefault().lookup(PortFactory.class);
  }

  public String getComPort()
  {
    return properties.get(BootloaderFactory.PROP_CONNECTION);
  }

  public int getBaudrate()
  {
    try {
      return Integer.parseInt(properties.getOrDefault(BootloaderFactory.PROP_SPEED,
                                                      "38400"));
    } catch (NumberFormatException ex) {
      log.log(Level.WARNING,
              "Cannot parse baudrate. Use default 38400.");
    }
    return 38400;
  }

  @Override
  public void connect() throws IOException
  {
    if (port == null) {
      try {
        SerialPort p = portFactory.createSerialPort(getComPort());
        try {
          p.setSerialPortParams(getBaudrate(),
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
          p.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
          out = Channels.newChannel(p.getOutputStream());
          in = Channels.newChannel(p.getInputStream());
          port = p;
          p = null;
        } finally {
          if (p != null) {
            p.close();
          }
        }
      } catch (PortInUseException | UnsupportedCommOperationException ex) {
        port = null;
        in = null;
        out = null;
        throw new IOException(ex);
      }
    }
  }

  @Override
  public boolean isConnected()
  {
    return port != null;
  }

  @Override
  public String getConnection()
  {
    return getComPort();
  }

  @Override
  public void close() throws IOException
  {
    try {
      try {
        if (in != null) {
          in.close();
        }
      } finally {
        if (out != null) {
          out.close();
        }
      }
    } finally {
      if (port != null) {
        port.close();
      }
      in = null;
      out = null;
      port = null;
    }
  }

}
