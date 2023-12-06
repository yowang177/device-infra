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

package com.google.devtools.mobileharness.shared.util.command.testing;

import com.google.devtools.mobileharness.shared.util.command.CommandResult;
import com.google.devtools.mobileharness.shared.util.command.CommandResults;

/** A util class for creating {@link CommandResult} for testing. */
public class FakeCommandResult {

  public static CommandResult of(String stdout, String stderr, int exitCode) {
    return CommandResults.of(
        stdout, stderr, exitCode, /* isTimeout= */ false, /* isStopped= */ false);
  }

  public static CommandResult of(
      String stdout, String stderr, int exitCode, boolean isTimeout, boolean isStopped) {
    return CommandResults.of(stdout, stderr, exitCode, isTimeout, isStopped);
  }

  private FakeCommandResult() {}
}
