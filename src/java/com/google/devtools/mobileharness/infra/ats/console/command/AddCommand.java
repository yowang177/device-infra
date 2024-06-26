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

package com.google.devtools.mobileharness.infra.ats.console.command;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.mobileharness.api.model.error.MobileHarnessException;
import com.google.devtools.mobileharness.infra.ats.console.ConsoleInfo;
import com.google.devtools.mobileharness.infra.ats.console.util.command.CommandHelper;
import com.google.devtools.mobileharness.infra.ats.console.util.console.ConsoleUtil;
import com.google.devtools.mobileharness.platform.android.xts.suite.subplan.AddSubPlanArgs;
import com.google.devtools.mobileharness.platform.android.xts.suite.subplan.ResultType;
import com.google.devtools.mobileharness.platform.android.xts.suite.subplan.SubPlanCreator;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/** Command for "add" commands. */
@Command(
    name = "add",
    sortOptions = false,
    description = "Create a subplan derived from previous session.",
    synopsisSubcommandLabel = "",
    subcommands = {
      HelpCommand.class,
    })
class AddCommand implements Callable<Integer> {
  @Spec private CommandSpec spec;

  private final ConsoleInfo consoleInfo;
  private final CommandHelper commandHelper;
  private final ConsoleUtil consoleUtil;
  private final SubPlanCreator subPlanCreator;

  @Inject
  AddCommand(
      ConsoleInfo consoleInfo,
      CommandHelper commandHelper,
      ConsoleUtil consoleUtil,
      SubPlanCreator subPlanCreator) {
    this.consoleInfo = consoleInfo;
    this.commandHelper = commandHelper;
    this.consoleUtil = consoleUtil;
    this.subPlanCreator = subPlanCreator;
  }

  @Override
  public Integer call() {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }

  @Command(
      name = "subplan",
      aliases = {"s"},
      description =
          "Create a subplan derived from previous session; this option generates a subplan that can"
              + " be used to run a subset of tests.")
  public int subPlan(
      @Option(
              names = "--session",
              required = true,
              description = "The session used to create a subplan.")
          int sessionIndex,
      @Option(
              names = {"--name", "-n"},
              required = false,
              description = "The name of the new subplan.")
          String name,
      @Option(
              names = "--result-type",
              required = false,
              description =
                  "Which results to include in the subplan. One of: ${COMPLETION-CANDIDATES}."
                      + " Repeatable.",
              split = "\\s*,\\s*")
          List<ResultType> resultTypes,
      @Option(
              names = {"--module", "-m"},
              required = false,
              description = "The test module to run.")
          String module,
      @Option(
              names = {"--is-non-tradefed-module"},
              required = false,
              description = "Whether the given module is for Non-Tradefed. False by default.")
          boolean isNonTradefedModule,
      @Option(
              names = {"--test", "-t"},
              required = false,
              description =
                  "The test to run, combined with the given module. Only works when module"
                      + " specified.")
          String test,
      @Option(
              names = {"--abi", "-a"},
              required = false,
              description =
                  "The abi to test, combined with the given module. Only works when module"
                      + " specified.")
          String abi,
      @Option(
              names = {"--exclude-filter"},
              paramLabel = "\"[abi] <module_name> <test_name>\"",
              description =
                  "Exclude the specified modules, or test packages, classes, and cases, from the"
                      + " subplan. For example, add subplan --session"
                      + " <session_index> --exclude-filter \"CtsCalendarcommon2Test"
                      + " android.calendarcommon2.cts.Calendarcommon2Test#testStaticLinking\""
                      + " excludes the specified test case.")
          List<String> excludeFilters)
      throws MobileHarnessException {
    Path xtsRootDirectory = consoleInfo.getXtsRootDirectoryNonEmpty();
    String xtsType = commandHelper.getXtsType();

    AddSubPlanArgs.Builder addSubPlanArgsBuilder =
        AddSubPlanArgs.builder()
            .setXtsRootDir(xtsRootDirectory)
            .setXtsType(xtsType)
            .setSessionIndex(sessionIndex);
    if (!Strings.isNullOrEmpty(name)) {
      addSubPlanArgsBuilder.setSubPlanName(name);
    }
    if (resultTypes != null) {
      addSubPlanArgsBuilder.setResultTypes(ImmutableSet.copyOf(resultTypes));
    }
    if (!Strings.isNullOrEmpty(module)) {
      addSubPlanArgsBuilder.setModule(module);
    }
    if (isNonTradefedModule) {
      addSubPlanArgsBuilder.setIsNonTradefedModule(true);
    }
    if (!Strings.isNullOrEmpty(test)) {
      addSubPlanArgsBuilder.setTest(test);
    }
    if (!Strings.isNullOrEmpty(abi)) {
      addSubPlanArgsBuilder.setAbi(abi);
    }
    if (excludeFilters != null) {
      addSubPlanArgsBuilder.setPassedInExcludeFilters(ImmutableSet.copyOf(excludeFilters));
    }

    Optional<File> subPlanFile =
        subPlanCreator.createAndSerializeSubPlan(addSubPlanArgsBuilder.build());
    if (subPlanFile.isPresent()) {
      consoleUtil.printlnStdout("Created subplan file at: %s", subPlanFile.get());
    } else {
      consoleUtil.printlnStdout("Subplan file not created");
    }
    return ExitCode.OK;
  }
}
