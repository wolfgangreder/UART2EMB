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
package at.or.reder.jboot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.openide.util.Lookup;

/**
 *
 * @author Wolfgang Reder
 */
public interface Bootloader extends AutoCloseable, Lookup.Provider
{

  public String getConnection();

  public void connect() throws IOException;

  public boolean isConnected();

  public Set<BootloaderFeature> getFeatures();

  public boolean upload(MemorySpace memorySpace,
                        InputStream is,
                        BootloaderListener listener) throws IOException, UnsupportedOperationException;

  public boolean download(MemorySpace memorySpace,
                          OutputStream out,
                          BootloaderListener listener) throws IOException, UnsupportedOperationException;

  public default boolean verify(MemorySpace memroySpace,
                                InputStream toVerify,
                                BootloaderListener listener) throws IOException, UnsupportedOperationException
  {
    return verify(memroySpace,
                  toVerify,
                  null,
                  listener);
  }

  public default boolean verify(MemorySpace memorySpace,
                                InputStream toVerify,
                                Predicate<CompareResult> compareSink,
                                BootloaderListener listener) throws IOException, UnsupportedOperationException
  {
    final AtomicBoolean success = new AtomicBoolean(true);
    final String runId = UUID.randomUUID().toString();
    final Predicate<CompareResult> compare;
    if (compareSink != null) {
      compare = compareSink.or(c -> c.getProvided() == -1);
    } else {
      compare = c -> c.getProvided() == 1;
    }
    try {
      download(memorySpace,
               new OutputStream()
       {
         private long position;

         @Override
         public void write(int b) throws IOException
         {
           int read = toVerify.read();
           if (!(read != -1 && read == b)) {
             success.set(false);
             if (!compare.test(CompareResult.builder()
                     .expected(read)
                     .provided(b)
                     .position(position).build())) {
               throw new IOException(runId);
             }
           }
           ++position;
         }

       },
               listener);
    } catch (IOException ex) {
      if (!runId.equals(ex.getMessage())) {
        throw ex;
      }
    }
    return success.get();
  }

  public default Optional<DeviceSignature> readSignature() throws IOException, UnsupportedOperationException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    download(MemorySpace.SIGNATURE,
             os,
             null);
    if (os.size() > 0) {
      return Optional.of(new DeviceSignature(ByteBuffer.wrap(os.toByteArray())));
    }
    return Optional.empty();
  }

  @Override
  public void close() throws IOException;

  @Override
  public default Lookup getLookup()
  {
    return Lookup.EMPTY;
  }

}
