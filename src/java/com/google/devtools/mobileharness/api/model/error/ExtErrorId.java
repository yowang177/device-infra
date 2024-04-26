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

package com.google.devtools.mobileharness.api.model.error;

import com.google.common.base.Preconditions;
import com.google.devtools.common.metrics.stability.model.proto.ErrorTypeProto.ErrorType;
import com.google.devtools.common.metrics.stability.util.ErrorIdFormatter;

/**
 * Extended error IDs for other Mobile Harness platform(iOS, mobly, etc) supports except Android,
 * like utilities, Driver/Decorator, Detector/Device implementations. For Android platform related,
 * check/update in {@link AndroidErrorId} instead.
 */
public enum ExtErrorId implements ErrorId {
  // ***********************************************************************************************
  // iOS: 60_001 ~ 80_000
  // ***********************************************************************************************

  // Mobly: 81_001 ~ 81_100
  MOBLY_SPONGE_OUTPUT_PARSING_ERROR(81_001, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_SUBDEVICE_CALLABLE_ERROR(81_002, ErrorType.INFRA_ISSUE),
  MOBLY_TESTBED_ADHOC_DRIVER_END_WITH_UNKNOWN_RESULT(81_003, ErrorType.INFRA_ISSUE),
  MOBLY_FAILED_TO_FIND_CONFIG_FILE_ERROR(81_004, ErrorType.CUSTOMER_ISSUE),
  MOBLY_FAILED_TO_READ_TESTBED_CONFIG_ERROR(81_005, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_CONFIG_MISSING_KEY_ERROR(81_006, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_CONFIG_PARSING_ERROR(81_007, ErrorType.CUSTOMER_ISSUE),
  MOBLY_INVALID_TESTBED_MODEL_CONFIG_ERROR(81_008, ErrorType.CUSTOMER_ISSUE),
  MOBLY_INVALID_TESTBED_CONTROLLER_CONFIG_ERROR(81_009, ErrorType.CUSTOMER_ISSUE),
  MOBLY_CONFIG_GENERATION_ERROR(81_010, ErrorType.CUSTOMER_ISSUE),
  MOBLY_FAILED_TO_CREATE_TEMP_DIRECTORY_ERROR(81_011, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_NAME_EMPTY_ERROR(81_012, ErrorType.CUSTOMER_ISSUE),
  MOBLY_EXECUTE_ERROR(81_013, ErrorType.UNDETERMINED),
  MOBLY_OUTPUT_PARSING_ERROR(81_014, ErrorType.INFRA_ISSUE),
  MOBLY_MISC_TESTBED_SUBDEVICE_JSON_TYPE_NAME_ERROR(81_015, ErrorType.INFRA_ISSUE),
  MOBLY_SUBDEVICE_TYPE_NOT_FOUND_ERROR(81_016, ErrorType.INFRA_ISSUE),
  MOBLY_TESTBED_ADHOC_DRIVER_SUBTEST_WITH_INFRA_ERROR_RESULT(81_017, ErrorType.INFRA_ISSUE),
  MOBLY_TESTBED_ADHOC_DRIVER_SUBTEST_WITH_DEPENDENCY_ERROR_RESULT(
      81_018, ErrorType.DEPENDENCY_ISSUE),
  MOBLY_TESTBED_ADHOC_DRIVER_SUBTEST_WITH_CUSTOMER_ERROR_RESULT(81_019, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_ADHOC_DRIVER_SUBTEST_WITH_UNDETERMINED_ERROR_RESULT(81_020, ErrorType.UNDETERMINED),
  MOBLY_TESTBED_ADHOC_DRIVER_SUBTEST_WITH_FAIL_RESULT(81_021, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_FAILURE(81_022, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_TIMEOUT(81_023, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_SUMMARY_YAML_PARSING_ERROR(81_024, ErrorType.INFRA_ISSUE),
  MOBLY_TEST_SCRIPT_ERROR(81_025, ErrorType.CUSTOMER_ISSUE),
  MOBLY_COMMAND_OUTPUT_EMPTY(81_026, ErrorType.INFRA_ISSUE),
  MOBLY_FAILED_TO_READ_COMMAND_OUTPUT(81_027, ErrorType.INFRA_ISSUE),
  MOBLY_TEST_CASE_FAILURE(81_028, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_CASE_ERROR(81_029, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_CASE_SKIPPED(81_030, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_SUMMARY_YAML_CONVERT_XML_ERROR(81_031, ErrorType.INFRA_ISSUE),
  MOBLY_SPONGE_XML_MISSING_ERROR(81_032, ErrorType.CUSTOMER_ISSUE),
  MOBLY_SDK_TOOL_NOT_FOUND_ERROR(81_033, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TEST_SUMMARY_YAML_MISSING_ERROR(81_034, ErrorType.CUSTOMER_ISSUE),
  MOBLY_TESTBED_REBOOT_ERROR(81_035, ErrorType.INFRA_ISSUE),

  MOBLY_AOSP_CREATE_VENV_ERROR(81_081, ErrorType.INFRA_ISSUE),
  MOBLY_AOSP_PYTHON_VERSION_NOT_FOUND_ERROR(81_082, ErrorType.INFRA_ISSUE),
  MOBLY_AOSP_UNZIP_TEST_PACKAGE_ERROR(81_083, ErrorType.CUSTOMER_ISSUE),
  MOBLY_AOSP_RESOLVE_TEST_PATH_ERROR(81_084, ErrorType.CUSTOMER_ISSUE),
  MOBLY_AOSP_PIP_INSTALL_ERROR(81_085, ErrorType.CUSTOMER_ISSUE),
  MOBLY_AOSP_MISSING_OUTPUT_ERROR(81_086, ErrorType.INFRA_ISSUE),
  MOBLY_AOSP_SPONGE_CONVERTER_ERROR(81_087, ErrorType.INFRA_ISSUE),

  // NoOpDriver: 83_401 ~ 83_420
  NO_OP_DRIVER_NON_PASSING_RESULT_SET_BY_MESSAGE(83_401, ErrorType.CUSTOMER_ISSUE),
  NO_OP_DRIVER_NON_PASSING_RESULT_SET_BY_PARAM(83_402, ErrorType.CUSTOMER_ISSUE),

  // TestBeds: 84_501 - 84_600
  TESTBED_CONFIG_TESTBED_ID_NOT_EXIST_ERROR(84_501, ErrorType.CUSTOMER_ISSUE),
  TESTBED_CONFIG_SUBDEVICE_NOT_EXIST_ERROR(84_502, ErrorType.CUSTOMER_ISSUE),
  TESTBED_CONFIG_TESTBED_SUBDEVICE_SAME_NAME_ERROR(84_503, ErrorType.CUSTOMER_ISSUE),
  TESTBED_PARSING_TYPE_NOT_EXIST_ERROR(84_504, ErrorType.CUSTOMER_ISSUE),
  TESTBED_PARSING_ID_NOT_EXIST_ERROR(84_505, ErrorType.CUSTOMER_ISSUE),
  TESTBED_PARSING_NAME_NOT_EXIST_ERROR(84_506, ErrorType.CUSTOMER_ISSUE),
  TESTBED_PARSING_DEVICE_NOT_EXIST_ERROR(84_507, ErrorType.CUSTOMER_ISSUE),
  TESTBED_PARSING_DEVICE_KEY_NOT_EXIST_ERROR(84_508, ErrorType.CUSTOMER_ISSUE),
  TESTBED_CONFIG_SUBDEVICE_ID_EMPTY_ERROR(84_509, ErrorType.CUSTOMER_ISSUE),
  TESTBED_YAML_PARSING_ERROR(84_510, ErrorType.CUSTOMER_ISSUE),
  TESTBED_CONFIG_DUPLICATE_ID_ERROR(84_511, ErrorType.CUSTOMER_ISSUE),
  TESTBED_DEVICE_DECORATOR_NOT_COMPATIBLE_ERROR(84_512, ErrorType.CUSTOMER_ISSUE),
  TESTBED_DEVICE_DECORATOR_SETUP_ERROR(84_513, ErrorType.CUSTOMER_ISSUE),

  // xTS Utils: 86_901 ~ 88_900
  // CompatibilityReportParser: 86_901 ~ 86_950
  REPORT_PARSER_READ_XML_FILE_ERROR(86_901, ErrorType.INFRA_ISSUE),
  REPORT_PARSER_XML_EVENT_READER_ERROR(86_902, ErrorType.INFRA_ISSUE),

  // CompatibilityReportMerger: 86_951 ~ 87_000
  REPORT_MERGER_PARSE_REPORTS_TIMEOUT_ERROR(86_951, ErrorType.INFRA_ISSUE),
  REPORT_MERGER_PARSE_REPORTS_GENERIC_ERROR(86_952, ErrorType.INFRA_ISSUE),
  REPORT_MERGER_PARSE_MOBLY_REPORTS_TIMEOUT_ERROR(86_953, ErrorType.INFRA_ISSUE),
  REPORT_MERGER_PARSE_MOBLY_REPORTS_GENERIC_ERROR(86_954, ErrorType.INFRA_ISSUE),

  // CompatibilityReportCreator: 87_001 ~ 87_050
  REPORT_CREATOR_WRITE_REPORT_TO_XML_ERROR(87_001, ErrorType.INFRA_ISSUE),

  // MoblyReportParser: 87_051 ~ 87_100
  MOBLY_REPORT_PARSER_PARSE_SUMMARY_FILE_ERROR(87_051, ErrorType.INFRA_ISSUE),

  // TestRecordHelper: 87_101 ~ 87_150
  TEST_RECORD_PARSE_PROTOS_TIMEOUT_ERROR(87_101, ErrorType.INFRA_ISSUE),
  TEST_RECORD_PARSE_PROTOS_GENERIC_ERROR(87_102, ErrorType.INFRA_ISSUE),
  TEST_RECORD_READ_PROTO_FILE_ERROR(87_103, ErrorType.INFRA_ISSUE),
  TEST_RECORD_WRITE_PROTO_FILE_ERROR(87_104, ErrorType.INFRA_ISSUE),
  TEST_RECORD_INVALID_PROTO_CONTEXT(87_105, ErrorType.INFRA_ISSUE),

  // xTS Configuration: 87_151 ~ 87_200
  MODULE_CONFIG_DEVICE_NUMBER_NOT_MATCH(87_151, ErrorType.CUSTOMER_ISSUE),
  MODULE_CONFIG_DRIVER_NOT_MATCH(87_152, ErrorType.CUSTOMER_ISSUE),
  MODULE_CONFIG_UNRECOGNIZED_OPTION_ERROR(87_153, ErrorType.CUSTOMER_ISSUE),

  // TestSuiteHelper: 87_201 ~ 87_250
  TEST_SUITE_ROOT_DIR_NOT_EXIST(87_201, ErrorType.DEPENDENCY_ISSUE),
  TEST_SUITE_DEVICE_ABIS_MISSING(87_202, ErrorType.INFRA_ISSUE),
  TEST_SUITE_NO_ABIS_SUPPORTED(87_203, ErrorType.DEPENDENCY_ISSUE),

  // AbiUtil: 87_251 ~ 87_300
  ABI_UTIL_GET_HOST_ABI_ERROR(87_251, ErrorType.DEPENDENCY_ISSUE),

  // SuiteModuleLoader: 87_301 ~ 87_350
  SUITE_MODULE_LOADER_DUPLICATED_FAMILY_MEMBERS(87_301, ErrorType.CUSTOMER_ISSUE),

  // CommandHelper: 87_351 ~ 87_400
  COMMAND_HELPER_XTS_ROOT_DIR_NOT_EXIST(87_351, ErrorType.DEPENDENCY_ISSUE),
  COMMAND_HELPER_ANDROID_XTS_DIR_NOT_EXIST(87_352, ErrorType.DEPENDENCY_ISSUE),
  COMMAND_HELPER_UNEXPECTED_XTS_TYPE(87_353, ErrorType.DEPENDENCY_ISSUE),

  // AbstractXmlParser: 87_401 ~ 87_410
  ABSTRACT_XML_PARSER_PARSE_XML_ERROR(87_401, ErrorType.INFRA_ISSUE),

  // PreviousResultLoader: 87_411 ~ 87_440
  PREV_RESULT_LOADER_SESSION_INDEX_OUT_OF_RANGE(87_411, ErrorType.CUSTOMER_ISSUE),
  PREV_RESULT_LOADER_NO_PREVIOUS_SESSION_FOUND(87_412, ErrorType.CUSTOMER_ISSUE),
  PREV_RESULT_LOADER_MISSING_TEST_RESULT_PROTO_FILE_IN_SESSION(87_413, ErrorType.INFRA_ISSUE),
  PREV_RESULT_LOADER_LOAD_TEST_RESULT_PROTO_FILE_ERROR(87_414, ErrorType.INFRA_ISSUE),

  // SubPlanCreator: 87_441 ~ 87_450
  SUBPLAN_CREATOR_SUBPLAN_FILE_ALREADY_EXISTED(87_441, ErrorType.CUSTOMER_ISSUE),

  EXT_PLACE_HOLDER_TO_BE_RENAMED(100_000, ErrorType.UNDETERMINED);

  public static final int MIN_CODE = InfraErrorId.MAX_CODE + 1;
  public static final int MAX_CODE = 100_000;

  private final int code;
  private final ErrorType type;

  ExtErrorId(int code, ErrorType type) {
    Preconditions.checkArgument(code >= MIN_CODE);
    Preconditions.checkArgument(code <= MAX_CODE);
    Preconditions.checkArgument(type != ErrorType.UNCLASSIFIED);
    this.code = code;
    this.type = type;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public ErrorType type() {
    return type;
  }

  @Override
  public String toString() {
    return ErrorIdFormatter.formatErrorId(this);
  }
}
