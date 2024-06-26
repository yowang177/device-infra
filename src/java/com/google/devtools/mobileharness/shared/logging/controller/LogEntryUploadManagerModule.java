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

package com.google.devtools.mobileharness.shared.logging.controller;

import com.google.devtools.mobileharness.shared.logging.controller.queue.LogEntryQueue;
import com.google.devtools.mobileharness.shared.logging.parameter.LogUploaderParameters;
import com.google.inject.AbstractModule;

/** Module for {@link LogEntryUploadManager}. */
public class LogEntryUploadManagerModule extends AbstractModule {

  private final LogUploaderParameters logUploaderParameters;

  public LogEntryUploadManagerModule(LogUploaderParameters logUploaderParameters) {
    this.logUploaderParameters = logUploaderParameters;
  }

  @Override
  protected void configure() {
    requireBinding(LogEntryQueue.class);
    install(logUploaderParameters.getLogUploaderModule());
  }
}
