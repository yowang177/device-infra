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

package com.google.wireless.qa.mobileharness.shared.api.validator.job;

import com.google.wireless.qa.mobileharness.shared.api.spec.AndroidCleanAppsSpec;
import com.google.wireless.qa.mobileharness.shared.model.job.JobInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Job validator for the {@code AndroidCleanAppsDecorator}. */
public class AndroidCleanAppsDecoratorJobValidator implements JobValidator {

  @Override
  public List<String> validate(JobInfo job) {
    List<String> errors = new ArrayList<>();
    String regexValue;
    if ((regexValue = job.params().get(AndroidCleanAppsSpec.PARAM_PKGS_TO_KEEP_REGEX, null))
        != null) {
      try {
        Pattern.compile(regexValue);
      } catch (PatternSyntaxException e) {
        errors.add("Invalid pattern syntax for value of param pkgs_to_keep_regex.");
      }
    }
    return errors;
  }
}
