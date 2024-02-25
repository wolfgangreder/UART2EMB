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

import at.or.reder.jboot.impl.jboot.Record;
import at.or.reder.jboot.impl.jboot.Response;
import at.or.reder.jboot.io.util.Crc16;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import lombok.NonNull;

/**
 *
 * @author Wolfgang Reder
 */
public class BootloaderStub
{

  private final class MyInputStream extends InputStream
  {

    @Override
    public int read() throws IOException
    {
      if (response.hasRemaining()) {
        return response.get() & 0xff;
      }
      return -1;
    }

  }

  private final class MyOutputStream extends OutputStream
  {

    @Override
    public void write(int b) throws IOException
    {
      if (request.hasRemaining()) {
        request.put((byte) b);
        if (!request.hasRemaining()) {
          request.rewind();
          processCommand();
          response.rewind();
        }
      } else {
        throw new IOException("Buffer overflow");
      }
    }

  }

  public static BootloaderStub aTmega8()
  {
    return aTmega8(0x2000);
  }

  public static BootloaderStub aTmega8(int bootloaderStart)
  {
    return new BootloaderStub(ByteOrder.LITTLE_ENDIAN,
                              bootloaderStart,
                              512,
                              64);
  }

  private static final byte SOH = 0x01;
  private static final byte EOT = 0x04;
  private static final int sizeOfCommandRec = 0x07;
  private final ByteBuffer flash;
  private final ByteBuffer eeprom;
  private final ByteBuffer buffer;
  private final int flashPageSize;
  private boolean endCommandLoop;
  private int address;
  private final ByteBuffer request;
  private final ByteBuffer response;

  public BootloaderStub(@NonNull ByteOrder byteOrder,
                        int flashSize,
                        int eepromSize,
                        int flashPageSize)
  {
    flash = ByteBuffer.allocate(flashSize).order(byteOrder);
    eeprom = ByteBuffer.allocate(eepromSize).order(byteOrder);
    this.flashPageSize = flashPageSize;
    buffer = ByteBuffer.allocate(flashPageSize).order(byteOrder);
    Arrays.fill(flash.array(),
                (byte) 0xff);
    Arrays.fill(eeprom.array(),
                (byte) 0xff);
    Arrays.fill(buffer.array(),
                (byte) 0);
    endCommandLoop = false;
    address = 0;
    request = ByteBuffer.allocate(Record.RECORDSIZE).order(byteOrder);
    response = ByteBuffer.allocate(Record.RECORDSIZE).order(byteOrder);
  }

  private boolean processCommand()
  {
    response.rewind();
    if (request.get(0) == SOH && request.get(sizeOfCommandRec - 1) == EOT) {
      Record requestRec = new Record(request);
      Record responseRec = new Record(response);
      response.put(0,
                   SOH);
      response.put(sizeOfCommandRec - 1,
                   EOT);
      endCommandLoop = innerProcessCommand(requestRec,
                                           responseRec);
    }
    request.rewind();
    return endCommandLoop;
  }

  private void boot_program_page(int pageAddress,
                                 ByteBuffer buffer)
  {
    buffer.rewind();
    flash.position(pageAddress * flashPageSize);
    flash.put(buffer);
    buffer.rewind();
  }

  private boolean innerProcessCommand(Record request,
                                      Record response)
  {
    boolean result = false;
    short checkSum;

    switch (request.getCommand()) {
      case CMD_NOP:
        response.setResponse(Response.RSP_OK_BOOTLOADER);
        response.setWordValue(calcProgramChecksum());
        break;
      case CMD_CHECK_PROGRAM:
        checkSum = calcProgramChecksum();
        response.setResponse(checkSum == request.getWordValue() ? Response.RSP_OK_BOOTLOADER : Response.RSP_ERR);
        break;
      case CMD_REBOOT:
        result = true;
        response.setResponse(Response.RSP_OK_BOOTLOADER);
        break;
      case CMD_ENTER_BOOTLOADER:
        address = 0;
        buffer.rewind();
        response.setResponse(Response.RSP_OK_BOOTLOADER);
        break;
      case CMD_PROGRAM:
        address += 2;
        buffer.putShort(request.getWordValue());
        if (!buffer.hasRemaining()) {
          boot_program_page((address - 1) / flashPageSize,
                            buffer);
          buffer.rewind();
        }
        if (address == flash.capacity()) {
          checkSum = calcProgramChecksum();
          eeprom.putShort(eeprom.capacity() - 2,
                          checkSum);
          response.setResponse(Response.RSP_DONE);
        } else {
          response.setResponse(Response.RSP_OK_BOOTLOADER);
        }
        break;
      case CMD_READ_SIGNATURE:
        response.setResponse(Response.RSP_ERR);
        //        memset( & command -> data,
        //               0,
        //               sizeof(command -> data));
        //        readSignature(command);
        //        command -> cmd = RSP_OK_BOOTLOADER;
        break;
      case CMD_READ_EEPROM:
        if (request.getAddress() >= eeprom.capacity() - 2) {
          response.setResponse(Response.RSP_ERR);
        } else {
          response.setResponse(Response.RSP_OK_BOOTLOADER);
          response.setByteValue(eeprom.get(request.getMemValue()));
        }
        break;
      case CMD_WRITE_EEPROM:
        if (request.getAddress() >= eeprom.capacity() - 2) {
          response.setResponse(Response.RSP_ERR);
        } else {
          response.setResponse(Response.RSP_OK_BOOTLOADER);
          eeprom.put(request.getAddress(),
                     (byte) request.getMemValue());
        }
        break;
      default:
        response.setResponse(Response.RSP_ERR);
    }
    return result;
  }

  private short calcProgramChecksum()
  {
    Crc16 crc = new Crc16();
    crc.update(flash.array());
    return (short) crc.getDigest();
  }

  public InputStream getInput()
  {
    return new MyInputStream();
  }

  public OutputStream getOutput()
  {
    return new MyOutputStream();
  }

  public ByteBuffer getFlash()
  {
    return flash.asReadOnlyBuffer().rewind();
  }

  public ByteBuffer getEEprom()
  {
    return eeprom.asReadOnlyBuffer().rewind();
  }

  public boolean isEndCommandLoop()
  {
    return endCommandLoop;
  }

}
