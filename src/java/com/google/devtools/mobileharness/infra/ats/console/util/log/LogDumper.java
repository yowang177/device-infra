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

package com.google.devtools.mobileharness.infra.ats.console.util.log;

import com.google.devtools.mobileharness.infra.ats.console.ConsoleUtil;
import com.google.devtools.mobileharness.infra.ats.console.constant.AtsConsoleDirs;
import com.google.devtools.mobileharness.infra.client.longrunningservice.constant.OlcServerDirs;

/** Utility for dumping logs. */
public class LogDumper {

  /** Prints information about log dirs of ATS console and OLC server. */
  public static void dumpLog(ConsoleUtil consoleUtil) {
    consoleUtil.printlnStdout("Saved log to %s", OlcServerDirs.getLatestLogFile());
    consoleUtil.printlnStdout("Saved log to %s", AtsConsoleDirs.getLatestLogFile());
  }

  private LogDumper() {}
}