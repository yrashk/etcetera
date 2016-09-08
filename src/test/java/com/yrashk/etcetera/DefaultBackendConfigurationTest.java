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

import javax.inject.Inject;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DefaultBackendConfigurationTest {

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return ContainerConfiguration.withDefaultConfig();
    }

    @Test
    public void hasEtc() throws InvalidSyntaxException {
        Collection<ServiceReference<ConfigBackend>> references = bundleContext
                .getServiceReferences(ConfigBackend.class, "(&(name=etc)(service=file))");
        assertFalse(references.isEmpty());
        assertEquals(1, references.size());
    }

    @Test
    public void etcHasCorrectPath() throws InvalidSyntaxException {
        Collection<ServiceReference<ConfigBackend>> references = bundleContext
                .getServiceReferences(ConfigBackend.class, "(&(name=etc)(path=" + System.getProperty("karaf.etc") + "))");
        assertFalse(references.isEmpty());
        assertEquals(1, references.size());
    }

}