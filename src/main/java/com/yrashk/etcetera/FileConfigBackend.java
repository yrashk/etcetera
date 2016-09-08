/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component(scope = ServiceScope.PROTOTYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class FileConfigBackend implements ConfigBackend {

    @Override public Collection<String> getFilenames() {
        try {
            return Files.walk(new File(path).toPath()).
                    filter(path -> path.toFile().isFile()).
                    map(Path::toString).
                    collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override public InputStream getFileInputStream(String name) throws IOException {
        return new FileInputStream(name);
    }

    private String path;
    private String name;
    private int order;

    public FileConfigBackend(String path, String name, int order) {
        this.path = path;
        this.name = name;
        this.order = order;
    }

    @Activate
    protected void activate(ComponentContext context) {
        System.out.println("context = " + context.getProperties());
    }

    @Override public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    @Override public String toString() {
        return "FileConfigBackend{name=" + name + ", path=" + path + "}";
    }
}
