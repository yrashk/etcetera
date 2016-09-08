/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface ConfigBackend {
    Collection<String> getFilenames();

    InputStream getFileInputStream(String name) throws IOException;
    void save(String name, String content) throws IOException;

    int getOrder();
    String getName();
}
