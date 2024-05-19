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

package com.google.wireless.qa.mobileharness.shared.api.device;

import com.google.devtools.mobileharness.api.model.error.MobileHarnessException;

/**
 * A virtual device which acts as a placeholder for the device that has no supported representation
 * in Mobile Harness (e.g., a callbox, attenuators, Tiger Lcd, etc.).
 */
public class MiscDevice extends BaseDevice {

  public MiscDevice(String deviceId) {
    super(deviceId);
  }

  @Override
  public void setUp() throws MobileHarnessException, InterruptedException {
    super.setUp();
    addSupportedDeviceType(this.getClass().getSimpleName());
    addSupportedDriver("NoOpDriver");
  }
}
