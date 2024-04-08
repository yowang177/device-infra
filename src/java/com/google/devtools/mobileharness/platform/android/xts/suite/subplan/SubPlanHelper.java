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

package com.google.devtools.mobileharness.platform.android.xts.suite.subplan;

import com.google.common.collect.ImmutableSet;
import com.google.devtools.mobileharness.infra.ats.console.result.proto.ReportProto.Module;
import com.google.devtools.mobileharness.infra.ats.console.result.proto.ReportProto.Result;
import com.google.devtools.mobileharness.infra.ats.console.result.proto.ReportProto.Test;
import com.google.devtools.mobileharness.infra.ats.console.result.proto.ReportProto.TestCase;
import com.google.devtools.mobileharness.platform.android.xts.suite.SuiteTestFilter;
import com.google.devtools.mobileharness.platform.android.xts.suite.retry.RetryResultHelper;
import java.util.Set;

/** SubPlan helper class. */
public class SubPlanHelper {

  /**
   * Creates a subplan per given previous result.
   *
   * <p>The given types can only be passed, failed or not_executed.
   */
  public static SubPlan createSubPlanForPreviousResult(
      Result previousResult,
      Set<String> types,
      boolean addSubPlanCmd,
      ImmutableSet<SuiteTestFilter> includeFilters,
      ImmutableSet<SuiteTestFilter> excludeFilters) {
    SubPlan subPlan = new SubPlan();
    for (Module module : previousResult.getModuleInfoList()) {
      if (RetryResultHelper.shouldRunModule(module, types, addSubPlanCmd)) {
        boolean isNonTfModule = module.getIsNonTfModule();
        if (RetryResultHelper.shouldRunEntireModule(
            module, types, addSubPlanCmd, includeFilters, excludeFilters)) {
          if (isNonTfModule) {
            subPlan.addNonTfIncludeFilter(
                String.format("%s %s", module.getAbi(), module.getName()));
          } else {
            subPlan.addIncludeFilter(String.format("%s %s", module.getAbi(), module.getName()));
          }
        } else { // If not run the entire module
          for (TestCase testCase : module.getTestCaseList()) {
            for (Test test : testCase.getTestList()) {
              if (RetryResultHelper.shouldRunTest(test, types, addSubPlanCmd)) {
                if (isNonTfModule) {
                  subPlan.addNonTfIncludeFilter(
                      String.format(
                          "%s %s %s#%s",
                          module.getAbi(), module.getName(), testCase.getName(), test.getName()));
                } else {
                  subPlan.addIncludeFilter(
                      String.format(
                          "%s %s %s#%s",
                          module.getAbi(), module.getName(), testCase.getName(), test.getName()));
                }
              }
            }
          }
        }
      }
    }
    return subPlan;
  }

  private SubPlanHelper() {}
}