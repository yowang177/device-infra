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

package com.google.devtools.mobileharness.platform.android.xts.suite;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Splitter;
import com.google.devtools.mobileharness.platform.android.xts.common.util.AbiUtil;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Represents a filter for including and excluding tests. */
@AutoValue
public abstract class SuiteTestFilter {

  /** The original filter string in --include-filter or --exclude-filter. */
  public abstract String filterString();

  public abstract Optional<String> abi();

  /**
   * Module name with module parameter (if any), e.g., "FooModuleName" or "FooModuleName[instant]".
   */
  public abstract String moduleName();

  public abstract Optional<String> testName();

  private static final Splitter FILTER_STRING_SPLITTER = Splitter.on(' ').omitEmptyStrings();

  private static final Pattern PARAMETERIZED_MODULE_REGEX = Pattern.compile("(.*)\\[(.*)\\]$");

  public static SuiteTestFilter create(String filterString) {
    List<String> tokens = FILTER_STRING_SPLITTER.splitToList(filterString);
    if (tokens.size() == 1) {
      return create(
          filterString, /* abi= */ null, /* moduleName= */ tokens.get(0), /* testName= */ null);
    } else {
      // Collect test name by removing abi and module name from the filter string as test names
      // may contain parameters with spaces.
      if (AbiUtil.isAbiSupportedByCompatibility(tokens.get(0))) {
        // Create a filter with abi.
        String abi = tokens.get(0);
        String moduleName = tokens.get(1);
        String testName = "";
        if (tokens.size() > 2) {
          testName = String.join(" ", tokens.subList(2, tokens.size()));
          ;
        }
        return create(filterString, abi, moduleName, testName.isEmpty() ? null : testName);
      } else {
        // Create a filter without abi.
        String moduleName = tokens.get(0);
        String testName = "";
        if (tokens.size() > 1) {
          testName = String.join(" ", tokens.subList(1, tokens.size()));
        }
        return create(
            filterString, /* abi= */ null, moduleName, testName.isEmpty() ? null : testName);
      }
    }
  }

  private static SuiteTestFilter create(
      String filterString, @Nullable String abi, String moduleName, @Nullable String testName) {
    return new AutoValue_SuiteTestFilter(
        filterString, Optional.ofNullable(abi), moduleName, Optional.ofNullable(testName));
  }

  /**
   * Exactly matches {@code originalModuleName} and {@code moduleParameter}. If {@link #abi()} is
   * empty, matches any {@code moduleAbi}. Otherwise exactly matches {@code moduleAbi}.
   */
  public boolean matchModule(
      String originalModuleName, @Nullable String moduleAbi, @Nullable String moduleParameter) {
    String moduleName =
        moduleParameter == null
            ? originalModuleName
            : String.format("%s[%s]", originalModuleName, moduleParameter);
    if (!moduleName().equals(moduleName)) {
      return false;
    }
    // If current SuiteTestFilter abi is empty, matching is true as long as the module names match
    if (abi().isEmpty()) {
      return true;
    }
    return abi().get().equals(moduleAbi);
  }

  /** Matches the given {@code moduleName} and ignoring the module parameter if any. */
  public boolean matchModuleName(String moduleName) {
    return getBaseModuleName().equals(moduleName);
  }

  @Memoized
  @Override
  public String toString() {
    return filterString();
  }

  /** Returns the base module name without the module parameter. */
  private String getBaseModuleName() {
    Matcher matcher = PARAMETERIZED_MODULE_REGEX.matcher(moduleName());
    if (matcher.matches()) {
      return matcher.group(1);
    }
    // No module parameter, return the original module name.
    return moduleName();
  }
}
