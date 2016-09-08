/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.apache.felix.utils.properties.InterpolationHelper;
import org.osgi.framework.BundleContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigFileReader {

    private final String name;
    private final InputStream inputStream;
    private final BundleContext bundleContext;

    private ConfigFileReader(String name, InputStream inputStream, BundleContext bundleContext) {
        this.name = name;
        this.inputStream = new BufferedInputStream(inputStream);
        this.bundleContext = bundleContext;
    }

    private Map<String, String> read() throws IOException {
        Map<String, String> config = new HashMap<>();
        if (name.endsWith(".cfg")) {
            final Properties p = new Properties();
            inputStream.mark(1);
            boolean isXml = inputStream.read() == '<';
            inputStream.reset();
            if (isXml) {
                p.loadFromXML(inputStream);
            } else {
                p.load(inputStream);
            }
            p.forEach((key, value) -> config.put(key.toString(), value.toString()));
            InterpolationHelper.performSubstitution(config, bundleContext);
        }
        inputStream.close();
        return config;
    }

    public static Map<String, String> read(String name, InputStream inputStream, BundleContext bundleContext) throws IOException {
        return new ConfigFileReader(name, inputStream, bundleContext).read();
    }

    public static Map<String, String> read(String name, InputStream inputStream) throws IOException {
        return new ConfigFileReader(name, inputStream, null).read();
    }


}
