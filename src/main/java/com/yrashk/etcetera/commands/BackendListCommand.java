/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera.commands;

import com.yrashk.etcetera.ConfigBackend;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Command(scope = "etcetera", name = "backends", description = "List configuration backends")
@Service
public class BackendListCommand implements Action {
    public static final List<String> SYSTEM_KEYS = Arrays.asList("order", "name", "service", "save", "objectClass",
                                                                 "service.bundleid", "service.id", "service.scope");

    @Override public Object execute() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(BackendListCommand.class).getBundleContext();
        List<ServiceReference<ConfigBackend>> references = new ArrayList<>(context
                .getServiceReferences(ConfigBackend.class, "(order=*)"));

        // Build the table
        ShellTable table = new ShellTable();

        table.column("Order").alignRight();
        table.column("Name").alignLeft();
        table.column("Service").alignLeft();
        table.column("Configuration").alignLeft();
        table.column("Save Policy").alignLeft();
        table.emptyTableText("No backends available");

        references.sort((x, y) -> ((Integer) x.getProperty("order")).compareTo((Integer) y.getProperty("order")));

        for (ServiceReference<ConfigBackend> reference : references) {
            ConfigBackend service = context.getService(reference);
            int order = (int) reference.getProperty("order");
            String name = (String) reference.getProperty("name");
            String svc = (String) reference.getProperty("service");
            String savePolicy = (String) reference.getProperty("save");
            if (savePolicy == null) {
                savePolicy = "NONE";
            }
            List<String> cfg = new ArrayList<>();
            for (String key : reference.getPropertyKeys()) {
                if (!SYSTEM_KEYS.contains(key)) {
                    cfg.add(key + "=" + reference.getProperty(key));
                }
            }
            table.addRow().addContent(order, name, svc, cfg.stream().collect(Collectors.joining(", ")), savePolicy);
            context.ungetService(reference);
        }

        // Print it
        table.print(System.out);
        return null;
    }
}
