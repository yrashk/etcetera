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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BackendConfigurationTest {

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() throws IOException {
        return ContainerConfiguration.withDefaultConfig(
                editConfigurationFilePut("etc/etcetera.properties", "backends",
                                         "base, main, overlay"),
                editConfigurationFilePut("etc/etcetera.properties", "base.service", "file"),
                editConfigurationFilePut("etc/etcetera.properties", "base.path", "lib"),
                editConfigurationFilePut("etc/etcetera.properties", "base.save", "ALL"),
                editConfigurationFilePut("etc/etcetera.properties", "main.service", "file"),
                editConfigurationFilePut("etc/etcetera.properties", "main.path", "bin"),
                editConfigurationFilePut("etc/etcetera.properties", "main.save", "FILES"),
                editConfigurationFilePut("etc/etcetera.properties", "overlay.service", "file"),
                editConfigurationFilePut("etc/etcetera.properties", "overlay.path", "${karaf.data}"),
                editConfigurationFilePut("etc/etcetera.properties", "overlay.save", "PROPERTIES"),
                editConfigurationFilePut("bin/test.cfg", "test1", "value1"),
                editConfigurationFilePut("bin/test.cfg", "test2", "value1"),
                editConfigurationFilePut("data/test.cfg", "test2", "value2"),
                editConfigurationFilePut("data/test-f.cfg", "test3", "value3")
        );
    }

    @Test
    public void backendsStarted() throws InvalidSyntaxException {
        Collection<ServiceReference<ConfigBackend>> references = bundleContext
                .getServiceReferences(ConfigBackend.class,
                                      "(&(name=base)(path=lib)(order=0))");
        assertFalse(references.isEmpty());
        assertEquals(1, references.size());

        references = bundleContext
                .getServiceReferences(ConfigBackend.class,
                                      "(&(name=main)(path=bin)(order=1))");
        assertFalse(references.isEmpty());
        assertEquals(1, references.size());

        references = bundleContext
                .getServiceReferences(ConfigBackend.class,
                                      "(&(name=overlay)(path=" + System.getProperty("karaf.data") + ")(order=2))");

        assertFalse(references.isEmpty());
        assertEquals(1, references.size());
    }

    @Test
    public void backendConfigsAreUsed() throws IOException {
        ServiceReference<ConfigurationAdmin> reference = bundleContext
                .getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);

        Dictionary<String, Object> properties = configurationAdmin.getConfiguration("test").getProperties();

        assertNotNull(properties);

        assertEquals("value1", properties.get("test1"));
        assertEquals("value2", properties.get("test2"));

        bundleContext.ungetService(reference);
    }

    @Test
    public void factory() throws IOException, InvalidSyntaxException {
        ServiceReference<ConfigurationAdmin> reference = bundleContext
                .getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);

        org.osgi.service.cm.Configuration[] configurations = configurationAdmin
                .listConfigurations("(service.factoryPid=test)");
        assertNotNull(configurations);

        Dictionary<String, Object> properties = configurations[0].getProperties();

        assertNotNull(properties);

        assertEquals("value3", properties.get("test3"));

        bundleContext.ungetService(reference);
    }

    @Test(timeout = 5000)
    public void updating() throws IOException {
        ServiceReference<ConfigurationAdmin> reference = bundleContext
                .getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);

        org.osgi.service.cm.Configuration configuration = configurationAdmin.getConfiguration("test");

        Hashtable<String, Object> update = new Hashtable<>();
        update.put("test2", "updated2");
        update.put("test3", "updated3");
        configuration.update(update);

        Map<String, String> config;

        do {
            config = ConfigFileReader.read("data/test.cfg", new FileInputStream("data/test.cfg"));
        } while (config.get("test2") == null || !config.get("test2").contentEquals("updated2"));

        // No other keys have leaked
        assertEquals(1, config.size());

        do {
            config = ConfigFileReader.read("bin/test.cfg", new FileInputStream("bin/test.cfg"));
        } while (config.get("test2") == null || !config.get("test2").contentEquals("updated2"));

        // Unknown keys should propagate here
        assertEquals("updated3", config.get("test3"));

    }

    @Test(timeout = 5000)
    public void creating() throws IOException {
        ServiceReference<ConfigurationAdmin> reference = bundleContext
                .getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);

        org.osgi.service.cm.Configuration configuration = configurationAdmin.getConfiguration("new");

        Hashtable<String, Object> update = new Hashtable<>();
        update.put("test", "passed");
        configuration.update(update);

        Map<String, String> config = new HashMap<>();

        do {
            try {
                config = ConfigFileReader.read("lib/new.cfg", new FileInputStream("lib/new.cfg"));
            } catch (IOException e) {}
        } while (config.get("test") == null || !config.get("test").contentEquals("passed"));

    }

    @Test
    public void defaultBackendNotStarted() throws InvalidSyntaxException {
        Collection<ServiceReference<ConfigBackend>> references = bundleContext
                .getServiceReferences(ConfigBackend.class, "(name=etc)");

        assertTrue(references.isEmpty());
    }

}