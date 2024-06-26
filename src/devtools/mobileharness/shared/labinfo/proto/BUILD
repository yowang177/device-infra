# Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

load("@com_github_grpc_grpc//bazel:python_rules.bzl", "py_grpc_library")
load("@com_google_protobuf//:protobuf.bzl", "py_proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//:deviceinfra_all_pkg",
        "//wireless/android/pixel/testtools/djar:__subpackages__",
    ],
)

proto_library(
    name = "lab_info_service_proto",
    srcs = ["lab_info_service.proto"],
    deps = [
        "//src/devtools/mobileharness/api/query/proto:lab_query_proto",
    ],
)

java_proto_library(
    name = "lab_info_service_java_proto",
    deps = [":lab_info_service_proto"],
)

java_grpc_library(
    name = "lab_info_service_java_grpc_proto",
    srcs = [":lab_info_service_proto"],
    deps = [":lab_info_service_java_proto"],
)

py_proto_library(
    name = "lab_info_service_py_proto",
    srcs = [
        "lab_info_service.proto",
    ],
    deps = [
        "//src/devtools/mobileharness/api/query/proto:lab_query_py_proto",
    ],
)

py_grpc_library(
    name = "lab_info_service_py_grpc",
    srcs = [":lab_info_service_proto"],
    tags = ["deviceinfra_disable_in_kokoro"],
    deps = [":lab_info_service_py_proto"],
)
