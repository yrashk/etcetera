/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ConfigFileReaderTest {

    private final byte[] PROPERTIES = "value = value".getBytes();
    private final byte[] PROPERTIES_XML = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties><entry key=\"value\">value</entry></properties>").getBytes();
    private final byte[] PROPERTIES_SUBSTITUTION = "value = value\nanother = ${value}".getBytes();
    private final byte[] PROPERTIES_CONTEXT = "value = ${karaf.etc}".getBytes();
    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return ContainerConfiguration.withDefaultConfig();
    }

    @Test
    public void propertiesFile() throws IOException {
        Map<String, String> config = ConfigFileReader.read("properties.cfg",
                                                           new ByteArrayInputStream(PROPERTIES));
        assertEquals("value", config.get("value"));
    }

    @Test
    public void propertiesSubstitution() throws IOException {
        Map<String, String> config = ConfigFileReader.read("properties.cfg",
                                                           new ByteArrayInputStream(PROPERTIES_SUBSTITUTION));
        assertEquals("value", config.get("another"));
    }

    @Test
    public void propertiesXMLFile() throws IOException {
        Map<String, String> config = ConfigFileReader.read("properties.cfg",
                                                           new ByteArrayInputStream(PROPERTIES_XML));
        assertEquals("value", config.get("value"));
    }

    @Test
    public void propertiesContext() throws IOException {
        Map<String, String> config = ConfigFileReader.read("properties.cfg",
                                                           new ByteArrayInputStream(PROPERTIES_CONTEXT), bundleContext);
        assertEquals(bundleContext.getProperty("karaf.etc"), config.get("value"));
    }
}