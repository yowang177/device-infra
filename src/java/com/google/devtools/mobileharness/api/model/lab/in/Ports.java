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

package com.google.devtools.mobileharness.api.model.lab.in;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableMap;
import com.google.devtools.mobileharness.api.model.proto.Lab;
import com.google.devtools.mobileharness.api.model.proto.Lab.LabPort;
import com.google.devtools.mobileharness.api.model.proto.Lab.PortType;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.concurrent.GuardedBy;

/** Server port information for locating a Mobile Harness lab. */
public class Ports {

  @GuardedBy("this")
  private final Map<PortType, Integer> ports = new HashMap<>();

  /** Sets the port number for the given type. */
  @CanIgnoreReturnValue
  public synchronized Ports add(PortType type, int port) {
    ports.put(type, port);
    return this;
  }

  /** Sets all the given ports. */
  @CanIgnoreReturnValue
  public synchronized Ports addAll(Map<PortType, Integer> ports) {
    this.ports.putAll(ports);
    return this;
  }

  /** Sets all the given ports. */
  @CanIgnoreReturnValue
  public synchronized Ports addAll(List<LabPort> protos) {
    protos.forEach(proto -> add(proto.getType(), proto.getNum()));
    return this;
  }

  /** Returns whether the port map is empty. */
  public synchronized boolean isEmpty() {
    return ports.isEmpty();
  }

  /** Returns the number of ports. */
  public synchronized int size() {
    return ports.size();
  }

  /** Gets the port number of the given type. If not exist, returns empty. */
  public synchronized Optional<Integer> get(PortType type) {
    return Optional.ofNullable(ports.get(type));
  }

  /** See {@link #get(PortType)}. */
  public int getNonNull(PortType type) {
    return get(type).orElseThrow();
  }

  /** Gets all the ports of this lab. */
  public synchronized Map<PortType, Integer> getAll() {
    return ImmutableMap.copyOf(ports);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Ports)) {
      return false;
    }
    Ports that = (Ports) obj;
    return getAll().equals(that.getAll());
  }

  @Override
  public synchronized int hashCode() {
    return Objects.hashCode(ports);
  }

  // Does not memorize this. Lab ports can be added afterwards.
  public synchronized List<LabPort> toProtos() {
    return ports.entrySet().stream()
        .map(e -> Lab.LabPort.newBuilder().setType(e.getKey()).setNum(e.getValue()).build())
        .collect(toImmutableList());
  }

  @Override
  public String toString() {
    return getAll().toString();
  }
}
