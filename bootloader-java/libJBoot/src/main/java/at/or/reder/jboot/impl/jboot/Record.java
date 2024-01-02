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
final class Record
{

  private static final byte SOH = (byte) 0x01;
  private static final byte EOT = (byte) 0x04;
  private final ByteBuffer buffer = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN);

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
    Record result = new Record(Commands.CMD_PROGRAM);
    result.setByteValue(data);
    return result;
  }

  public static Record commandCheckProgram(int crc)
  {
    Record result = new Record(Commands.CMD_CHECK_PROGRAM);
    result.setWordValue(crc & 0xffff);
    return result;
  }

  public static Record commandReboot()
  {
    return new Record(Commands.CMD_REBOOT);
  }

  public static Record commandReadSignature()
  {
    return new Record(Commands.CMD_READ_SIGNATURE);
  }

  Record(@NonNull Commands command)
  {
    buffer.array()[0] = SOH;
    buffer.array()[6] = EOT;
    setCommand(command);
  }

  public Response getResponse()
  {
    return Response.valueOfMagic(buffer.get(1)).orElse(Response.RSP_ERR);
  }

  public Commands getCommand()
  {
    return Commands.valueOfMagic(buffer.get(1)).orElse(null);
  }

  public void setCommand(@NonNull Commands command)
  {
    buffer.put(1,
               command.getMagic());
  }

  public int getByteValue()
  {
    return buffer.get(2) & 0xff;
  }

  public void setByteValue(int value)
  {
    buffer.put(2,
               (byte) (value & 0xff));
  }

  public short getWordValue()
  {
    return buffer.getShort(2);
  }

  public void setWordValue(int value)
  {
    buffer.putShort(2,
                    (short) (value & 0xffff));
  }

  public int getIntValue()
  {
    return buffer.getInt(2);
  }

  public void setIntValue(int value)
  {
    buffer.putInt(2,
                  value);
  }

  public ByteBuffer getData()
  {
    return buffer.slice(2,
                        4).duplicate();
  }

  public void setData(ByteBuffer bufferToCopy)
  {
    buffer.put(2,
               bufferToCopy,
               0,
               4);
  }

  public ByteBuffer getBuffer()
  {
    return buffer.asReadOnlyBuffer();
  }

}
