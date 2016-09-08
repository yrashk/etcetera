/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class ConfigWriter implements ConfigurationListener {

    private BundleContext context;

    private static class BackendPolicy {
        private final ConfigBackend backend;
        private final SavePolicy policy;

        private BackendPolicy(ConfigBackend backend, SavePolicy policy) {
            this.backend = backend;
            this.policy = policy;
        }

        public ConfigBackend getBackend() {
            return backend;
        }

        public SavePolicy getPolicy() {
            return policy;
        }
    }

    private List<BackendPolicy> backends = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policyOption = ReferencePolicyOption.GREEDY)
    protected void bindConfigBackend(ServiceReference<ConfigBackend> reference, ConfigBackend backend) {
        SavePolicy savePolicy = SavePolicy.NONE;
        if (reference.getProperty("save") != null) {
            savePolicy = SavePolicy.valueOf((String) reference.getProperty("save"));
        }
        backends.add(new BackendPolicy(backend, savePolicy));
        backends.sort((x, y) -> new Integer(x.getBackend().getOrder()).compareTo(y.getBackend().getOrder()));
    }

    protected void unbindConfigBackend(ServiceReference<ConfigBackend> reference, ConfigBackend backend) {
        backends.remove(backend);
    }

    @Reference
    private ConfigurationAdmin configurationAdmin;

    @Activate
    protected void activate(ComponentContext context) {
        this.context = context.getBundleContext();
    }

    @Override public void configurationEvent(ConfigurationEvent event) {
        if (event.getType() != ConfigurationEvent.CM_UPDATED) {
            return;
        }
        for (BackendPolicy backendPolicy : backends) {
            SavePolicy policy = backendPolicy.getPolicy();
            if (policy != SavePolicy.NONE) {
                ConfigBackend backend = backendPolicy.getBackend();
                String filename = event.getPid() + ".cfg";
                try {
                    Configuration config = configurationAdmin.getConfiguration(event.getPid(), event.getFactoryPid());
                    Dictionary dict = config.getProperties();
                    boolean fileExists = backend.getFilenames().stream().anyMatch(name -> name.contentEquals(filename));
                    if ((policy == SavePolicy.FILES && fileExists) ||
                         policy == SavePolicy.ALL) {
                        boolean isXML = false;
                        if (fileExists) {
                            try (InputStream fileInputStream = backend.getFileInputStream(filename)) {
                                isXML = fileInputStream.read() == '<';
                            }
                        }
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Properties props = new Properties();
                        for (Enumeration e = dict.keys(); e.hasMoreElements(); ) {
                            String key = e.nextElement().toString();
                            String value = dict.get(key).toString();
                            props.put(key, value);
                        }
                        if (isXML) {
                            props.storeToXML(bos, "");
                        } else {
                            props.store(bos, "");
                        }
                        backend.save(filename, bos.toString());
                    }
                    if (policy == SavePolicy.PROPERTIES && fileExists) {
                        boolean isXML;
                        try (InputStream fileInputStream = backend.getFileInputStream(filename)) {
                            isXML = fileInputStream.read() == '<';
                        }
                        Map<String, String> original;
                        try (InputStream fileInputStream = backend.getFileInputStream(filename)) {
                            original = ConfigFileReader.read(filename, fileInputStream, context);
                        }

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Properties props = new Properties();

                        for (Map.Entry<String, String> entry : original.entrySet()) {
                            Object newValue = dict.get(entry.getKey());
                            if (newValue == null) {
                                props.put(entry.getKey(), entry.getValue());
                            } else {
                                props.put(entry.getKey(), newValue);
                            }
                        }

                        if (isXML) {
                            props.storeToXML(bos, "");
                        } else {
                            props.store(bos, "");
                        }
                        backend.save(filename, bos.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
