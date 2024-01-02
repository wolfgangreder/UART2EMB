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

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Wolfgang Reder
 */
public interface BootloaderFactory
{

  public static final String PROP_CONNECTION = "at.or.reder.jboot.connection";
  public static final String PROP_SPEED = "at.or.reder.jboot.speed";

  public String getName();

  public UUID getId();

  public Bootloader createBootloader(Map<String, String> properties) throws IOException;

}
