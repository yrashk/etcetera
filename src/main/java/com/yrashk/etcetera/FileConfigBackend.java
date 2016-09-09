/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FileConfigBackend implements ConfigBackend {

    @Override public Collection<String> getFilenames() {
        try {
            return Files.walk(new File(path).toPath())
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .map(file -> file.toString().substring(path.length() + 1))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override public InputStream getFileInputStream(String name) throws IOException {
        return new FileInputStream(getFilename(name));
    }

    @Override public void save(String name, String content) throws IOException {
        FileWriter fileWriter = new FileWriter(getFilename(name));
        fileWriter.write(content);
        fileWriter.close();
    }

    private String getFilename(String name) {return path + File.separatorChar + name;}


    private String path;
    private String name;
    private int order;

    public FileConfigBackend(String path, String name, int order) {
        this.path = path;
        this.name = name;
        this.order = order;
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
