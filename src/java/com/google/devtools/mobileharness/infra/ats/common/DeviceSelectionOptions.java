/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.mobileharness.infra.ats.common;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;

/** Device selection options. */
@AutoValue
public abstract class DeviceSelectionOptions {

  /** Run test on specific devices with given serial number(s). */
  public abstract ImmutableList<String> serials();

  /** Run test on any device except those with given serial number(s). */
  public abstract ImmutableList<String> excludeSerials();

  /**
   * Run test on devices with this product type(s). May also filter by variant using
   * product:variant.
   */
  public abstract ImmutableList<String> productTypes();

  /** Run test on devices with given property values. */
  public abstract ImmutableMap<String, String> deviceProperties();

  /** Run test on devices with battery level no more than this value. */
  public abstract Optional<Integer> maxBatteryLevel();

  /** Run test on devices with battery level no less than this value. */
  public abstract Optional<Integer> minBatteryLevel();

  /** Run test on devices with battery temperature no more than this value. */
  public abstract Optional<Integer> maxBatteryTemperature();

  /** Run test on devices with SDK version no more than this value. */
  public abstract Optional<Integer> maxSdkLevel();

  /** Run test on devices with SDK version no less than this value. */
  public abstract Optional<Integer> minSdkLevel();

  public static Builder builder() {
    return new AutoValue_DeviceSelectionOptions.Builder()
        .setSerials(ImmutableList.of())
        .setExcludeSerials(ImmutableList.of())
        .setProductTypes(ImmutableList.of())
        .setDeviceProperties(ImmutableMap.of());
  }

  /** Builder for {@link DeviceSelectionOptions}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setSerials(ImmutableList<String> serials);

    public abstract Builder setExcludeSerials(ImmutableList<String> excludeSerials);

    public abstract Builder setProductTypes(ImmutableList<String> productTypes);

    public abstract Builder setDeviceProperties(ImmutableMap<String, String> deviceProperties);

    public abstract Builder setMaxBatteryLevel(int maxBatteryLevel);

    public abstract Builder setMinBatteryLevel(int minBatteryLevel);

    public abstract Builder setMaxBatteryTemperature(int maxBatteryTemperature);

    public abstract Builder setMaxSdkLevel(int maxSdkLevel);

    public abstract Builder setMinSdkLevel(int minSdkLevel);

    public abstract DeviceSelectionOptions build();
  }
}
