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

package com.google.devtools.mobileharness.infra.ats.common;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.devtools.mobileharness.infra.ats.console.controller.proto.SessionPluginProto.XtsType;
import com.google.devtools.mobileharness.infra.client.api.controller.device.DeviceQuerier;
import com.google.devtools.mobileharness.platform.android.xts.config.ConfigurationUtil;
import com.google.devtools.mobileharness.platform.android.xts.config.ModuleConfigurationHelper;
import com.google.devtools.mobileharness.shared.util.file.local.LocalFileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.google.wireless.qa.mobileharness.shared.proto.JobConfig;
import com.google.wireless.qa.mobileharness.shared.proto.JobConfig.StringMap;
import com.google.wireless.qa.mobileharness.shared.proto.JobConfig.SubDeviceSpec;
import com.google.wireless.qa.mobileharness.shared.proto.query.DeviceQuery.DeviceInfo;
import com.google.wireless.qa.mobileharness.shared.proto.query.DeviceQuery.DeviceQueryResult;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class SessionRequestHandlerUtilTest {
  private static final String XTS_ROOT_DIR_PATH = "/path/to/xts_root_dir";
  private static final String ANDROID_XTS_ZIP_PATH = "/path/to/android_xts.zip";

  @Rule public MockitoRule mockito = MockitoJUnit.rule();
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Bind @Mock private DeviceQuerier deviceQuerier;
  @Bind @Mock private LocalFileUtil localFileUtil;
  @Bind @Mock private ModuleConfigurationHelper moduleConfigurationHelper;
  @Bind @Mock private ConfigurationUtil configurationUtil;

  @Inject private SessionRequestHandlerUtil sessionRequestHandlerUtil;

  @Before
  public void doBeforeEachTest() {
    Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
  }

  @Test
  public void createXtsTradefedTestJobConfig_pickOneDevice() throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build());

    // Asserts the driver
    assertThat(jobConfigOpt.get().getDriver().getName()).isEqualTo("XtsTradefedTest");
    String driverParams = jobConfigOpt.get().getDriver().getParam();
    Map<String, String> driverParamsMap =
        new Gson().fromJson(driverParams, new TypeToken<Map<String, String>>() {});
    assertThat(driverParamsMap)
        .containsExactly(
            "xts_type", "CTS", "xts_root_dir", XTS_ROOT_DIR_PATH, "xts_test_plan", "cts");
  }

  @Test
  public void createXtsTradefedTestJobConfig_addAndroidXtsZipPathIfAvailable() throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setAndroidXtsZip(ANDROID_XTS_ZIP_PATH)
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build());

    // Asserts the driver
    assertThat(jobConfigOpt.get().getDriver().getName()).isEqualTo("XtsTradefedTest");
    String driverParams = jobConfigOpt.get().getDriver().getParam();
    Map<String, String> driverParamsMap =
        new Gson().fromJson(driverParams, new TypeToken<Map<String, String>>() {});
    assertThat(driverParamsMap)
        .containsExactly(
            "xts_type", "CTS", "android_xts_zip", ANDROID_XTS_ZIP_PATH, "xts_test_plan", "cts");
  }

  @Test
  public void createXtsTradefedTestJobConfig_shardCount2_pick2Devices() throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setShardCount(2)
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(
            SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build(),
            SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build());
  }

  @Test
  public void createXtsTradefedTestJobConfig_shardCount3_only2OnlineDevices_pick2Devices()
      throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setShardCount(3)
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(
            SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build(),
            SubDeviceSpec.newBuilder().setType("AndroidRealDevice").build());
  }

  @Test
  public void createXtsTradefedTestJobConfig_noOnlineDevices_noJobConfig() throws Exception {
    when(deviceQuerier.queryDevice(any())).thenReturn(DeviceQueryResult.getDefaultInstance());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isEmpty();
  }

  @Test
  public void createXtsTradefedTestJobConfig_withGivenSerial() throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setDeviceSerials(ImmutableList.of("device_id_1"))
                .setModuleNames(ImmutableList.of("module1"))
                .setShardCount(2)
                .setExtraArgs(ImmutableList.of("--logcat-on-failure"))
                .build(),
            ImmutableList.of("module1"));

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(
            SubDeviceSpec.newBuilder()
                .setType("AndroidRealDevice")
                .setDimensions(StringMap.newBuilder().putContent("serial", "device_id_1"))
                .build());

    // Asserts the driver
    assertThat(jobConfigOpt.get().getDriver().getName()).isEqualTo("XtsTradefedTest");
    String driverParams = jobConfigOpt.get().getDriver().getParam();
    Map<String, String> driverParamsMap =
        new Gson().fromJson(driverParams, new TypeToken<Map<String, String>>() {});
    assertThat(driverParamsMap)
        .containsExactly(
            "xts_type",
            "CTS",
            "xts_root_dir",
            XTS_ROOT_DIR_PATH,
            "xts_test_plan",
            "cts",
            "run_command_args",
            "-m module1 --shard-count 2 --logcat-on-failure");
  }

  @Test
  public void createXtsTradefedTestJobConfig_someGivenSerialsNotExist_pickExistingDevicesOnly()
      throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_3").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setDeviceSerials(
                    ImmutableList.of("device_id_1", "not_exist_device", "device_id_3"))
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isPresent();
    assertThat(jobConfigOpt.get().getDevice().getSubDeviceSpecList())
        .containsExactly(
            SubDeviceSpec.newBuilder()
                .setType("AndroidRealDevice")
                .setDimensions(StringMap.newBuilder().putContent("serial", "device_id_1"))
                .build(),
            SubDeviceSpec.newBuilder()
                .setType("AndroidRealDevice")
                .setDimensions(StringMap.newBuilder().putContent("serial", "device_id_3"))
                .build());
  }

  @Test
  public void createXtsTradefedTestJobConfig_allGivenSerialsNotExist_noJobConfig()
      throws Exception {
    when(deviceQuerier.queryDevice(any()))
        .thenReturn(
            DeviceQueryResult.newBuilder()
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_1").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_2").addType("AndroidOnlineDevice"))
                .addDeviceInfo(
                    DeviceInfo.newBuilder().setId("device_id_3").addType("AndroidOnlineDevice"))
                .build());

    Optional<JobConfig> jobConfigOpt =
        sessionRequestHandlerUtil.createXtsTradefedTestJobConfig(
            SessionRequestHandlerUtil.SessionRequestInfo.builder()
                .setTestPlan("cts")
                .setXtsType(XtsType.CTS)
                .setXtsRootDir(XTS_ROOT_DIR_PATH)
                .setDeviceSerials(ImmutableList.of("device_id_4", "device_id_5"))
                .build(),
            ImmutableList.of());

    assertThat(jobConfigOpt).isEmpty();
  }
}