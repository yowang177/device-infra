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

package com.google.devtools.mobileharness.platform.android.xts.config;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;
import com.google.devtools.mobileharness.api.model.error.InfraErrorId;
import com.google.devtools.mobileharness.api.model.error.MobileHarnessException;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.Configuration;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.ConfigurationDescriptor;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.ConfigurationDescriptorMetadata;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.ConfigurationMetadata;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.Device;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.Option;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.TargetPreparer;
import com.google.devtools.mobileharness.platform.android.xts.config.proto.ConfigurationProto.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** A parser for xts configuration files. */
public class ConfigurationXmlParser {
  private static final String DESCRIPTION = "description";
  private static final String OPTION = "option";
  private static final String NAME = "name";
  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final String DEVICE = "device";
  private static final String TEST = "test";
  private static final String CLASS = "class";
  private static final String TARGET_PREPARER = "target_preparer";
  private static final String CONFIG_DESCRIPTOR_METADATA_OPTION_NAME = "config-descriptor:metadata";

  public ConfigurationXmlParser() {}

  /**
   * Parses the {@code xml} stream and saves the result to the {@code Configuration} proto.
   *
   * @throws MobileHarnessException if fail to parse
   */
  public static Configuration parse(File xmlFile) throws MobileHarnessException {
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(xmlFile);
    } catch (IOException e) {
      throw new MobileHarnessException(
          InfraErrorId.XTS_CONFIG_XML_PARSE_ERROR,
          String.format("Failed to open configuration xml file: %s", xmlFile),
          e);
    }
    DocumentBuilder documentBuilder;
    Document document;
    Configuration.Builder configuration = Configuration.newBuilder();
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = documentBuilder.parse(fileInputStream);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new MobileHarnessException(
          InfraErrorId.XTS_CONFIG_XML_PARSE_ERROR, "Failed to parse configuration xml file", e);
    }

    Element root = document.getDocumentElement();
    String fileName = xmlFile.getName();
    String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
    configuration.setMetadata(
        ConfigurationMetadata.newBuilder()
            .setXtsModule(fileNameWithoutExtension)
            .setIsConfigV2(
                ConfigCommon.CONFIG_V2_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith)));

    configuration.setDescription(root.getAttribute(DESCRIPTION));
    ListMultimap<String, String> configDescriptorMetadata = LinkedListMultimap.create();

    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node node = root.getChildNodes().item(i);
      switch (node.getNodeName()) {
        case OPTION:
          Option option = parseOption(node);
          if (option.getName().equals(CONFIG_DESCRIPTOR_METADATA_OPTION_NAME)) {
            configDescriptorMetadata.put(option.getKey(), option.getValue());
          } else {
            configuration.addOptions(option);
          }
          break;
        case DEVICE:
          configuration.addDevices(parseDevice(node));
          break;
        case TEST:
          configuration.setTest(parseTest(node));
          break;
        default:
          break;
      }
    }

    configuration.setConfigDescriptor(
        ConfigurationDescriptor.newBuilder()
            .putAllMetadata(
                configDescriptorMetadata.asMap().entrySet().stream()
                    .collect(
                        toImmutableMap(
                            Entry::getKey,
                            e ->
                                ConfigurationDescriptorMetadata.newBuilder()
                                    .setKey(e.getKey())
                                    .addAllValue(e.getValue())
                                    .build()))));

    return configuration.build();
  }

  private static Option parseOption(Node root) {
    Option.Builder option = Option.newBuilder();
    for (int i = 0; i < root.getAttributes().getLength(); i++) {
      Node attribute = root.getAttributes().item(i);
      switch (attribute.getNodeName()) {
        case NAME:
          option.setName(attribute.getNodeValue());
          break;
        case KEY:
          option.setKey(attribute.getNodeValue());
          break;
        case VALUE:
          option.setValue(attribute.getNodeValue());
          break;
        default:
          break;
      }
    }
    return option.build();
  }

  private static Test parseTest(Node root) {
    Test.Builder test = Test.newBuilder();
    test.setClazz(((Element) root).getAttribute(CLASS));
    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node node = root.getChildNodes().item(i);
      switch (node.getNodeName()) {
        case OPTION:
          test.addOptions(parseOption(node));
          break;
        default:
          break;
      }
    }
    return test.build();
  }

  private static TargetPreparer parseTargetPreparer(Node root) {
    TargetPreparer.Builder targetPreparer = TargetPreparer.newBuilder();
    targetPreparer.setClazz(((Element) root).getAttribute(CLASS));
    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node node = root.getChildNodes().item(i);
      switch (node.getNodeName()) {
        case OPTION:
          targetPreparer.addOptions(parseOption(node));
          break;
        default:
          break;
      }
    }
    return targetPreparer.build();
  }

  private static Device parseDevice(Node root) {
    Device.Builder device = Device.newBuilder();
    device.setName(((Element) root).getAttribute(NAME));
    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node node = root.getChildNodes().item(i);
      switch (node.getNodeName()) {
        case TARGET_PREPARER:
          device.addTargetPreparers(parseTargetPreparer(node));
          break;
        case OPTION:
          device.addOptions(parseOption(node));
          break;
        default:
          break;
      }
    }
    return device.build();
  }
}
