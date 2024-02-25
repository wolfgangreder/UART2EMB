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

import at.or.reder.jboot.impl.jboot.Record;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.NonNull;

/**
 *
 * @author Wolfgang Reder
 */
public final class Record
{

  public static final int RECORDSIZE = 7;
  public static final byte SOH = (byte) 0x01;
  public static final byte EOT = (byte) 0x04;
  private final ByteBuffer buffer;

  public static Record commandNop()
  {
    return new Record(Commands.CMD_NOP);
  }

  public static Record commandEnterBootloader()
  {
    return new Record(Commands.CMD_ENTER_BOOTLOADER);
  }

  public static Record commandProgram(int data)
  {
    return new Record(Commands.CMD_PROGRAM).setWordValue(data & 0xffff);
  }

  public static Record commandCheckProgram(int crc)
  {
    return new Record(Commands.CMD_CHECK_PROGRAM).setWordValue(crc & 0xffff);
  }

  public static Record commandReboot()
  {
    Record result = new Record(Commands.CMD_REBOOT);
    result.getBuffer().put(1,
                           RebootType.REBOOT_NORMAL.getMagic());
    return result;
  }

  public static Record commandReboot(int crc)
  {
    Record result = new Record(Commands.CMD_REBOOT);
    result.getBuffer().put(2,
                           RebootType.REBOOT_CRC16.getMagic());
    result.getBuffer().putShort(3,
                                (short) crc);
    return result;
  }

  public static Record commandReadSignature()
  {
    return new Record(Commands.CMD_READ_SIGNATURE);
  }

  public Record()
  {
    buffer = ByteBuffer.allocate(RECORDSIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.array()[0] = SOH;
    buffer.array()[6] = EOT;
  }

  public Record(@NonNull Commands command)
  {
    buffer = ByteBuffer.allocate(RECORDSIZE).order(ByteOrder.LITTLE_ENDIAN);
    buffer.array()[0] = SOH;
    buffer.array()[6] = EOT;
    setCommand(command);
  }

  public Record(@NonNull ByteBuffer buffer)
  {
    if (buffer.remaining() < RECORDSIZE) {
      throw new IllegalArgumentException("invalid buffer size");
    }

    this.buffer = buffer.slice(buffer.position(),
                               RECORDSIZE)
            .duplicate()
            .order(ByteOrder.LITTLE_ENDIAN);
  }

  public Response getResponse()
  {
    return Response.valueOfMagic(buffer.get(1)).orElse(Response.RSP_ERR);
  }

  public Record setResponse(@NonNull Response response)
  {
    buffer.put(1,
               response.getMagic());
    return this;
  }

  public Commands getCommand()
  {
    return Commands.valueOfMagic(buffer.get(1)).orElse(null);
  }

  public Record setCommand(@NonNull Commands command)
  {
    buffer.put(1,
               command.getMagic());
    return this;
  }

  public int getByteValue()
  {
    return buffer.get(2) & 0xff;
  }

  public Record setByteValue(int value)
  {
    buffer.put(2,
               (byte) (value & 0xff));
    return this;
  }

  public short getWordValue()
  {
    return buffer.getShort(2);
  }

  public Record setWordValue(int value)
  {
    buffer.putShort(2,
                    (short) (value & 0xffff));
    return this;
  }

  public int getIntValue()
  {
    return buffer.getInt(2);
  }

  public Record setIntValue(int value)
  {
    buffer.putInt(2,
                  value);
    return this;
  }

  public ByteBuffer getData()
  {
    return buffer.slice(2,
                        4).duplicate().rewind();
  }

  public Record setData(ByteBuffer bufferToCopy)
  {
    buffer.put(2,
               bufferToCopy,
               0,
               4);
    return this;
  }

  public int getAddress()
  {
    return buffer.get(3) & 0xffff;
  }

  public Record setAddress(int address)
  {
    buffer.putShort(3,
                    (short) address);
    return this;
  }

  public int getMemValue()
  {
    return buffer.get(2) & 0xff;
  }

  public Record setMemValue(int memValue)
  {
    buffer.put(2,
               (byte) memValue);
    return this;
  }

  public ByteBuffer getBuffer()
  {
    return buffer;
  }

}
