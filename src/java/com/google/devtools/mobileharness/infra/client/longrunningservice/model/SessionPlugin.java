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

package com.google.devtools.mobileharness.infra.client.longrunningservice.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.devtools.mobileharness.shared.constant.closeable.NonThrowingAutoCloseable;
import com.google.devtools.mobileharness.shared.util.event.EventBusBackend.Subscriber;
import com.google.protobuf.Descriptors.Descriptor;

/** A loaded session plugin. */
@AutoValue
public abstract class SessionPlugin {

  public abstract SessionInfo sessionInfo();

  public abstract Subscriber subscriber();

  public abstract NonThrowingAutoCloseable closeableResource();

  public abstract ImmutableList<Descriptor> descriptors();

  public static SessionPlugin of(
      SessionInfo sessionInfo,
      Subscriber subscriber,
      NonThrowingAutoCloseable closeableResource,
      ImmutableList<Descriptor> descriptors) {
    return new AutoValue_SessionPlugin(sessionInfo, subscriber, closeableResource, descriptors);
  }
}
