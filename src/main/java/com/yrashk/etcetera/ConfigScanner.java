/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class ConfigScanner {

    private List<ConfigBackend> backends = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policyOption = ReferencePolicyOption.GREEDY)
    protected void bindConfigBackend(ConfigBackend backend) {
        backends.add(backend);
        backends.sort((x, y) -> new Integer(x.getOrder()).compareTo(y.getOrder()));
    }

    protected void unbindConfigBackend(ConfigBackend backend) {
        backends.remove(backend);
    }

    @Reference
    private ConfigurationAdmin configurationAdmin;

    @Activate
    protected void activate(ComponentContext context) throws IOException {
        for (ConfigBackend backend : backends) {
            for (String name : backend.getFilenames()) {
                InputStream fileInputStream = backend.getFileInputStream(name);
                Map<String, String> config = ConfigFileReader
                        .read(name, fileInputStream, context.getBundleContext());
                if (config.size() > 0) {
                    String factory = getFactory(name);
                    String pid = getPid(name);
                    Configuration configuration;
                    if (factory == null) {
                        configuration = configurationAdmin.getConfiguration(pid);
                    } else {
                        configuration = configurationAdmin.createFactoryConfiguration(pid);
                    }
                    @SuppressWarnings("unchecked")
                    Dictionary<String, Object> properties = configuration.getProperties();
                    if (properties == null) {
                        properties = new Hashtable<>();
                    }
                    config.forEach(properties::put);
                    configuration.update(properties);
                }
            }
        }
    }

    private String getPid(String name) {
        String pid = new File(name).getName();
        if (pid.indexOf(".") > 0) {
            pid = pid.substring(0, pid.lastIndexOf("."));
        }
        if (pid.indexOf("-") > 0) {
            pid = pid.substring(0, pid.indexOf("-"));
        }
        return pid;
    }

    private String getFactory(String name) {
        String pid = new File(name).getName();
        if (pid.indexOf(".") > 0) {
            pid = pid.substring(0, pid.lastIndexOf("."));
        }
        if (pid.indexOf("-") > 0) {
            return pid.substring(pid.indexOf("-") + 1);
        }
        return null;
    }

}
