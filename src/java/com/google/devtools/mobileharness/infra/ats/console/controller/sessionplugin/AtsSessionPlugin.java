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

package com.google.devtools.mobileharness.infra.ats.console.controller.sessionplugin;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.devtools.mobileharness.shared.constant.LogRecordImportance.IMPORTANCE;
import static com.google.devtools.mobileharness.shared.constant.LogRecordImportance.Importance.IMPORTANT;
import static com.google.devtools.mobileharness.shared.util.time.TimeUtils.toJavaDuration;
import static com.google.devtools.mobileharness.shared.util.time.TimeUtils.toProtoDuration;
import static com.google.devtools.mobileharness.shared.util.time.TimeUtils.toProtoTimestamp;
import static com.google.protobuf.TextFormat.shortDebugString;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.devtools.mobileharness.api.model.error.MobileHarnessException;
import com.google.devtools.mobileharness.api.model.job.out.Result.ResultTypeWithCause;
import com.google.devtools.mobileharness.api.model.proto.Test.TestResult;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.AtsSessionPluginConfig;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.AtsSessionPluginConfig.CommandCase;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.AtsSessionPluginOutput;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.AtsSessionPluginOutput.Failure;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.AtsSessionPluginOutput.ResultCase;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.DumpCommand;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.ListCommand;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.RunCommand;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.RunCommandState;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.RunCommandState.Invocation;
import com.google.devtools.mobileharness.infra.client.longrunningservice.constant.SessionProperties;
import com.google.devtools.mobileharness.infra.client.longrunningservice.model.SessionEndedEvent;
import com.google.devtools.mobileharness.infra.client.longrunningservice.model.SessionInfo;
import com.google.devtools.mobileharness.infra.client.longrunningservice.model.SessionStartingEvent;
import com.google.devtools.mobileharness.infra.client.longrunningservice.model.WithProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.wireless.qa.mobileharness.client.api.event.JobEndEvent;
import com.google.wireless.qa.mobileharness.shared.controller.event.TestEndedEvent;
import com.google.wireless.qa.mobileharness.shared.controller.event.TestStartingEvent;
import com.google.wireless.qa.mobileharness.shared.model.job.TestInfo;
import com.google.wireless.qa.mobileharness.shared.model.lab.DeviceLocator;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;

/** OmniLab long-running client session plugin for ATS console. */
@WithProto({AtsSessionPluginConfig.class, AtsSessionPluginOutput.class})
public class AtsSessionPlugin {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final AtomicInteger NEXT_RUN_COMMAND_ID = new AtomicInteger(1);

  private final Object tradefedJobsLock = new Object();

  @GuardedBy("tradefedJobsLock")
  private final Map<String, Boolean> runningTradefedJobs = new HashMap<>();

  private final SessionInfo sessionInfo;
  private final DumpEnvVarCommandHandler dumpEnvVarCommandHandler;
  private final DumpStackTraceCommandHandler dumpStackCommandHandler;
  private final DumpUptimeCommandHandler dumpUptimeCommandHandler;
  private final ListDevicesCommandHandler listDevicesCommandHandler;
  private final ListModulesCommandHandler listModulesCommandHandler;
  private final RunCommandHandler runCommandHandler;

  /** Set in {@link #onSessionStarting}. */
  private volatile AtsSessionPluginConfig config;

  @Inject
  AtsSessionPlugin(
      SessionInfo sessionInfo,
      DumpEnvVarCommandHandler dumpEnvVarCommandHandler,
      DumpStackTraceCommandHandler dumpStackCommandHandler,
      DumpUptimeCommandHandler dumpUptimeCommandHandler,
      ListDevicesCommandHandler listDevicesCommandHandler,
      ListModulesCommandHandler listModulesCommandHandler,
      RunCommandHandler runCommandHandler) {
    this.sessionInfo = sessionInfo;
    this.dumpEnvVarCommandHandler = dumpEnvVarCommandHandler;
    this.dumpStackCommandHandler = dumpStackCommandHandler;
    this.dumpUptimeCommandHandler = dumpUptimeCommandHandler;
    this.listDevicesCommandHandler = listDevicesCommandHandler;
    this.listModulesCommandHandler = listModulesCommandHandler;
    this.runCommandHandler = runCommandHandler;
  }

  @Subscribe
  public void onSessionStarting(SessionStartingEvent event)
      throws MobileHarnessException, InvalidProtocolBufferException, InterruptedException {
    config =
        sessionInfo
            .getSessionPluginExecutionConfig()
            .getConfig()
            .unpack(AtsSessionPluginConfig.class);
    logger.atInfo().log("Config: %s", shortDebugString(config));

    onSessionStarting();
  }

  private void onSessionStarting() throws MobileHarnessException, InterruptedException {
    if (config.getCommandCase().equals(CommandCase.RUN_COMMAND)) {
      RunCommand runCommand = config.getRunCommand();

      setRunCommandState(
          oldState ->
              runCommand.getInitialState().toBuilder()
                  .setCommandId(Integer.toString(NEXT_RUN_COMMAND_ID.getAndIncrement()))
                  .build());

      runCommandHandler.initialize(runCommand);
      ImmutableList<String> tradefedJobIds = runCommandHandler.addTradefedJobs(runCommand);
      synchronized (tradefedJobsLock) {
        for (String tradefedJobId : tradefedJobIds) {
          runningTradefedJobs.putIfAbsent(tradefedJobId, true);
        }
      }
      if (tradefedJobIds.isEmpty()) {
        logger.atInfo().log(
            "On session [%s] starting, no tradefed job was added, try add non-tradefed jobs if"
                + " needed.",
            sessionInfo.getSessionId());
        runCommandHandler.addNonTradefedJobs(runCommand);
      }
      return;
    } else if (config.getCommandCase().equals(CommandCase.LIST_COMMAND)) {
      ListCommand listCommand = config.getListCommand();
      if (listCommand.getCommandCase().equals(ListCommand.CommandCase.LIST_DEVICES_COMMAND)) {
        AtsSessionPluginOutput output =
            listDevicesCommandHandler.handle(listCommand.getListDevicesCommand());
        setOutput(output);
        return;
      } else if (listCommand
          .getCommandCase()
          .equals(ListCommand.CommandCase.LIST_MODULES_COMMAND)) {
        AtsSessionPluginOutput output =
            listModulesCommandHandler.handle(listCommand.getListModulesCommand());
        setOutput(output);
        return;
      }
    } else if (config.getCommandCase().equals(CommandCase.DUMP_COMMAND)) {
      DumpCommand dumpCommand = config.getDumpCommand();
      if (dumpCommand.getCommandCase().equals(DumpCommand.CommandCase.DUMP_STACK_TRACE_COMMAND)) {
        AtsSessionPluginOutput output =
            dumpStackCommandHandler.handle(dumpCommand.getDumpStackTraceCommand());
        setOutput(output);
        return;
      } else if (dumpCommand
          .getCommandCase()
          .equals(DumpCommand.CommandCase.DUMP_ENV_VAR_COMMAND)) {
        AtsSessionPluginOutput output =
            dumpEnvVarCommandHandler.handle(dumpCommand.getDumpEnvVarCommand());
        setOutput(output);
        return;
      } else if (dumpCommand.getCommandCase().equals(DumpCommand.CommandCase.DUMP_UPTIME_COMMAND)) {
        AtsSessionPluginOutput output =
            dumpUptimeCommandHandler.handle(dumpCommand.getDumpUptimeCommand());
        setOutput(output);
        return;
      }
    }
    setOutput(
        AtsSessionPluginOutput.newBuilder()
            .setFailure(
                Failure.newBuilder()
                    .setErrorMessage(String.format("Unimplemented [%s]", shortDebugString(config))))
            .build());
  }

  @Subscribe
  public void onSessionEnded(SessionEndedEvent event)
      throws MobileHarnessException, InterruptedException {
    // If the result has been set, returns immediately.
    if (sessionInfo
        .getSessionPluginOutput(AtsSessionPluginOutput.class)
        .map(output -> !output.getResultCase().equals(ResultCase.RESULT_NOT_SET))
        .orElse(false)) {
      return;
    }

    if (config.getCommandCase().equals(CommandCase.RUN_COMMAND)) {
      RunCommandState runCommandState = getRunCommandState();
      logger
          .atInfo()
          .with(IMPORTANCE, IMPORTANT)
          .log(
              "Command [%s] is done and start to handle result which may take several minutes"
                  + " based on the session scale.",
              runCommandState.getCommandId());
      runCommandHandler.handleResultProcessing(config.getRunCommand(), runCommandState);
    }
  }

  @Subscribe
  public void onJobEnd(JobEndEvent jobEndEvent)
      throws MobileHarnessException, InterruptedException {
    synchronized (tradefedJobsLock) {
      String jobId = jobEndEvent.getJob().locator().getId();
      if (!runningTradefedJobs.containsKey(jobId)) {
        return;
      }
      runningTradefedJobs.put(jobId, false);
      if (runningTradefedJobs.values().stream().noneMatch(running -> running)) {
        logger.atInfo().log(
            "All added tradefed jobs have been done, try add non-tradefed jobs if needed.");
        runCommandHandler.addNonTradefedJobs(config.getRunCommand());
      }
    }
  }

  @Subscribe
  public void onTestStarting(TestStartingEvent event) {
    ImmutableList<DeviceLocator> devices = event.getAllocation().getAllDeviceLocators();
    setRunCommandState(
        oldState ->
            oldState.toBuilder()
                .putRunningInvocation(
                    event.getTest().locator().getId(),
                    Invocation.newBuilder()
                        .setCommandId(oldState.getCommandId())
                        .setStartTime(toProtoTimestamp(Instant.now()))
                        .addAllDeviceId(
                            devices.stream()
                                .map(DeviceLocator::getSerial)
                                .collect(toImmutableList()))
                        .setStateSummary(config.getRunCommand().getTestPlan())
                        .build())
                .build());
    sessionInfo.putSessionProperty(
        SessionProperties.PROPERTY_KEY_SESSION_CONTAIN_STARTED_TEST, "true");
  }

  @Subscribe
  public void onTestEnded(TestEndedEvent event) {
    setRunCommandState(
        oldState ->
            oldState.toBuilder()
                .setTotalExecutionTime(
                    toProtoDuration(
                        toJavaDuration(oldState.getTotalExecutionTime())
                            .plus(
                                Duration.between(
                                    event.getTest().timing().getStartTime(), Instant.now()))))
                .removeRunningInvocation(event.getTest().locator().getId())
                .build());

    TestInfo testInfo = event.getTest();
    ResultTypeWithCause resultTypeWithCause = testInfo.resultWithCause().get();
    if (!resultTypeWithCause.type().equals(TestResult.PASS)) {
      logger
          .atWarning()
          .with(IMPORTANCE, IMPORTANT)
          .log(
              "Warning of test [%s/%s]:\n%s",
              testInfo.locator().getId(),
              testInfo.locator().getName(),
              resultTypeWithCause.toStringWithDetail());
    }
  }

  private void setOutput(AtsSessionPluginOutput output) {
    sessionInfo.setSessionPluginOutput(oldOutput -> output, AtsSessionPluginOutput.class);
    logger.atInfo().log("Output: %s", shortDebugString(output));
  }

  private void setRunCommandState(UnaryOperator<RunCommandState> runCommandStateUpdater) {
    sessionInfo.setSessionPluginOutput(
        oldOutput -> {
          AtsSessionPluginOutput.Builder newOutput =
              oldOutput == null ? AtsSessionPluginOutput.newBuilder() : oldOutput.toBuilder();
          RunCommandState oldState = newOutput.getRunCommandState();
          RunCommandState newState = runCommandStateUpdater.apply(oldState);
          return newOutput.setRunCommandState(newState).build();
        },
        AtsSessionPluginOutput.class);
  }

  private RunCommandState getRunCommandState() {
    return sessionInfo
        .getSessionPluginOutput(AtsSessionPluginOutput.class)
        .orElse(AtsSessionPluginOutput.getDefaultInstance())
        .getRunCommandState();
  }
}
