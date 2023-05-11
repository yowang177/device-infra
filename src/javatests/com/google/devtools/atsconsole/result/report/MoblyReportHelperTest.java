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

package com.google.devtools.atsconsole.result.report;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.devtools.atsconsole.result.proto.ReportProto.Attribute;
import com.google.devtools.atsconsole.result.proto.ReportProto.AttributeList;
import com.google.devtools.atsconsole.util.TestRunfilesUtil;
import com.google.devtools.deviceinfra.platform.android.lightning.internal.sdk.adb.Adb;
import com.google.devtools.mobileharness.shared.util.file.local.LocalFileUtil;
import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.google.protobuf.TextFormat;
import java.io.File;
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
public final class MoblyReportHelperTest {

  private static final String DEVICE_PROPERTIES_FILE =
      TestRunfilesUtil.getRunfilesLocation("result/report/testdata/mobly/device_properties.txt");

  private static final String DEVICE_ID = "11191FDD4002M4";

  private static final String DEVICE_KERNEL_INFO =
      "Linux localhost 4.19.160-g85b17737b5f2-ab7508408 #1 SMP PREEMPT Wed Jun 30 08:01:04 UTC 2021"
          + " aarch64";

  private static final ImmutableList<Attribute> EXPECTED_BUILD_ATTRIBUTES =
      ImmutableList.of(
          Attribute.newBuilder().setKey("build_abi").setValue("arm64-v8a").build(),
          Attribute.newBuilder().setKey("build_abi2").setValue("").build(),
          Attribute.newBuilder()
              .setKey("build_abis")
              .setValue("arm64-v8a,armeabi-v7a,armeabi")
              .build(),
          Attribute.newBuilder().setKey("build_abis_32").setValue("armeabi-v7a,armeabi").build(),
          Attribute.newBuilder().setKey("build_abis_64").setValue("arm64-v8a").build(),
          Attribute.newBuilder().setKey("build_board").setValue("redfin").build(),
          Attribute.newBuilder()
              .setKey("build_bootimage_fingerprint")
              .setValue("google/redfin/redfin:11/RQ3A.211001.001/7641976:userdebug/dev-keys")
              .build(),
          Attribute.newBuilder().setKey("build_brand").setValue("google").build(),
          Attribute.newBuilder().setKey("build_device").setValue("redfin").build(),
          Attribute.newBuilder()
              .setKey("build_fingerprint")
              .setValue("google/redfin/redfin:11/RQ3A.211001.001/7641976:userdebug/dev-keys")
              .build(),
          Attribute.newBuilder().setKey("build_id").setValue("RQ3A.211001.001").build(),
          Attribute.newBuilder().setKey("build_manufacturer").setValue("Google").build(),
          Attribute.newBuilder().setKey("build_model").setValue("Pixel 5").build(),
          Attribute.newBuilder().setKey("build_product").setValue("redfin").build(),
          Attribute.newBuilder().setKey("build_reference_fingerprint").setValue("").build(),
          Attribute.newBuilder().setKey("build_serial").setValue("11191FDD4002M4").build(),
          Attribute.newBuilder().setKey("build_tags").setValue("dev-keys").build(),
          Attribute.newBuilder().setKey("build_type").setValue("userdebug").build(),
          Attribute.newBuilder()
              .setKey("build_vendor_fingerprint")
              .setValue("google/redfin/redfin:11/RQ3A.211001.001/7641976:userdebug/dev-keys")
              .build(),
          Attribute.newBuilder().setKey("build_version_base_os").setValue("").build(),
          Attribute.newBuilder().setKey("build_version_incremental").setValue("7641976").build(),
          Attribute.newBuilder().setKey("build_version_release").setValue("11").build(),
          Attribute.newBuilder().setKey("build_version_sdk").setValue("30").build(),
          Attribute.newBuilder()
              .setKey("build_version_security_patch")
              .setValue("2021-10-01")
              .build(),
          Attribute.newBuilder()
              .setKey("system_img_info")
              .setValue("google/redfin/redfin:11/RQ3A.211001.001/7641976:userdebug/dev-keys")
              .build(),
          Attribute.newBuilder()
              .setKey("vendor_img_info")
              .setValue("google/redfin/redfin:11/RQ3A.211001.001/7641976:userdebug/dev-keys")
              .build(),
          Attribute.newBuilder().setKey("device_kernel_info").setValue(DEVICE_KERNEL_INFO).build());

  @Rule public final MockitoRule mocks = MockitoJUnit.rule();
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Bind @Mock private Adb adb;

  private LocalFileUtil localFileUtil;

  @Inject private MoblyReportHelper moblyReportHelper;

  @Before
  public void setUp() {
    Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    localFileUtil = new LocalFileUtil();
  }

  @Test
  public void generateBuildAttributes() throws Exception {
    String devicePropertiesOutput = localFileUtil.readFile(DEVICE_PROPERTIES_FILE);
    when(adb.runShellWithRetry(DEVICE_ID, "getprop")).thenReturn(devicePropertiesOutput);
    when(adb.runShellWithRetry(DEVICE_ID, "uname -a")).thenReturn(DEVICE_KERNEL_INFO);

    assertThat(moblyReportHelper.generateBuildAttributes(DEVICE_ID))
        .containsExactlyEntriesIn(
            EXPECTED_BUILD_ATTRIBUTES.stream()
                .collect(toImmutableMap(Attribute::getKey, Attribute::getValue)))
        .inOrder();
  }

  @Test
  public void generateBuildAttributesFile() throws Exception {
    File tempDir = temporaryFolder.newFolder("temp");

    String devicePropertiesOutput = localFileUtil.readFile(DEVICE_PROPERTIES_FILE);
    when(adb.runShellWithRetry(DEVICE_ID, "getprop")).thenReturn(devicePropertiesOutput);
    when(adb.runShellWithRetry(DEVICE_ID, "uname -a")).thenReturn(DEVICE_KERNEL_INFO);

    moblyReportHelper.generateBuildAttributesFile(DEVICE_ID, tempDir.toPath());

    AttributeList attributes =
        TextFormat.parse(
            localFileUtil.readFile(
                tempDir
                    .toPath()
                    .resolve(MoblyReportHelper.MOBLY_REPORT_BUILD_ATTR_TEXTPROTO_FILE_NAME)),
            AttributeList.class);
    assertThat(attributes.getAttributeList())
        .containsExactlyElementsIn(EXPECTED_BUILD_ATTRIBUTES)
        .inOrder();
  }
}
