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

import com.google.common.truth.Truth;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Wolfgang Reder
 */
public class BootloaderFeatureTest
{

  @Test
  public void testGetFromMask()
  {
    for (BootloaderFeature feature : BootloaderFeature.values()) {
      int mask = BootloaderFeature.createMask(Set.of(feature));
      Set<BootloaderFeature> fm = BootloaderFeature.getFromMask(mask);
      Truth.assertThat(fm.size()).isEqualTo(1);
      Truth.assertWithMessage("Testing %s",
                              feature.name()).that(fm.contains(feature)).isTrue();
    }
  }

  @Test
  public void testCreateMask()
  {
    Set<BootloaderFeature> set = EnumSet.allOf(BootloaderFeature.class);
    int mask = BootloaderFeature.createMask(set);
    Set<BootloaderFeature> result = BootloaderFeature.getFromMask(mask);
    Truth.assertThat(result).isEqualTo(set);
  }

}
