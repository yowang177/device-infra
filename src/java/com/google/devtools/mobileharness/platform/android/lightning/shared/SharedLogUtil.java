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

package com.google.devtools.mobileharness.platform.android.lightning.shared;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CompileTimeConstant;
import com.google.wireless.qa.mobileharness.shared.log.LogCollector;
import java.util.logging.Level;
import javax.annotation.Nullable;

/** Static logging utilities shared by Mobile Harness Lightning APIs. */
public final class SharedLogUtil {

  /**
   * Logs message at info level to {@code logger} and {@code log} if {@code log} is not {@code
   * null}.
   */
  public static void logMsg(
      FluentLogger logger, @CompileTimeConstant String message, @Nullable LogCollector<?> log) {
    logMsg(logger, message, Level.INFO, log, /* cause= */ null);
  }

  /**
   * Logs message at given {@code level} to {@code logger} and {@code log} if {@code log} is not
   * {@code null}.
   */
  public static void logMsg(
      FluentLogger logger,
      @CompileTimeConstant String message,
      Level level,
      @Nullable LogCollector<?> log,
      @Nullable Throwable cause) {
    if (log != null) {
      log.at(level).alsoTo(logger).withCause(cause).log(message);
    } else {
      logger.at(level).withCause(cause).log("%s", message);
    }
  }

  private SharedLogUtil() {}
}
