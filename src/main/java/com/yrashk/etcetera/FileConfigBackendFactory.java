/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Map;

@Component(immediate = true, scope = ServiceScope.SINGLETON, property = "type=file")
public class FileConfigBackendFactory implements ConfigBackendFactory {

    @Override public ConfigBackend build(Map<String, Object> properties) {
        return new FileConfigBackend((String)properties.get("path"),
                                     (String)properties.get("name"),
                                     (int)properties.get("order"));
    }
}
