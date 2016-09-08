/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class ConfigBackendProvisioner {

    private static final String KARAF_ETC = System.getProperty("karaf.etc");
    private static final String ETCETERA_PROPERTIES = KARAF_ETC + File.separatorChar + "etcetera.properties";

    @Reference
    protected ConfigurationAdmin configurationAdmin;

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policyOption = ReferencePolicyOption.GREEDY)
    protected List<ConfigBackendFactory> factories;

    private Set<ServiceRegistration<ConfigBackend>> registrations = new HashSet<>();

    @Activate
    protected void activate(ComponentContext context) {
        File propertiesFile = new File(ETCETERA_PROPERTIES);
        Properties props = new Properties();
        props.put("backends", "etc");
        props.put("etc.service", "file");
        props.put("etc.path", KARAF_ETC);
        if (propertiesFile.exists()) {
            try {
                props.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                throw new RuntimeException(ETCETERA_PROPERTIES + " can't be read");
            }
        }

        List<String> backends = Arrays.stream(props.getProperty("backends").split(","))
                                      .map(String::trim).collect(Collectors.toList());

        for (String backend : backends) {
            String service = props.getProperty(backend + ".service");

            String prefix = backend + ".";

            Map<String, Object> properties = props.keySet().stream()
                                               .filter(key -> key instanceof String && ((String) key)
                                                       .startsWith(prefix))
                                               .collect(Collectors.toMap((key) -> ((String)key).substring(prefix.length()),
                                                                         (key) -> props.getProperty((String) key)));
            properties.put("name", backend);

            Optional<ServiceReference<ConfigBackendFactory>> maybeBackendFactory = getBackendFactory(context, service);
            if (maybeBackendFactory.isPresent()) {
                properties.put("order", registrations.size());

                ServiceReference<ConfigBackendFactory> reference = maybeBackendFactory.get();
                ConfigBackendFactory factory = context.getBundleContext().getService(reference);
                ConfigBackend backendInstance = factory.build(properties);
                context.getBundleContext().ungetService(reference);

                ServiceRegistration<ConfigBackend> registration =
                        context.getBundleContext().registerService(ConfigBackend.class, backendInstance,
                                                                   new Hashtable<>(properties));

                registrations.add(registration);
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    private Optional<ServiceReference<ConfigBackendFactory>> getBackendFactory(ComponentContext context, String service) {
        Collection<ServiceReference<ConfigBackendFactory>> serviceReferences = null;
        String filter = "(type=" + service + ")";
        try {
            serviceReferences = context.getBundleContext().getServiceReferences(ConfigBackendFactory.class,
                                                                                filter);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Invalid filter syntax: " + filter);
        }
        if (serviceReferences.isEmpty()) {
            // silently skip, there might be a chance one will appear later
            return Optional.empty();
        } else if (serviceReferences.size() > 1) {
            throw new RuntimeException("Multiple ConfigBackendFactory services found for " + service);
        } else {
            return serviceReferences.stream().findFirst();
        }
    }

}
