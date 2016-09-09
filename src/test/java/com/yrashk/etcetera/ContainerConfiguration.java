/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel.*;

public class ContainerConfiguration {

    @SuppressWarnings("unused")
    public static final MavenUrlReference karafStandardRepo =
            maven()
                    .groupId("org.apache.karaf.features")
                    .artifactId("standard")
                    .version("4.0.6")
                    .classifier("features")
                    .type("xml");

    public static final MavenUrlReference etceteraRepo =
            maven()
                    .groupId("com.yrashk")
                    .artifactId("etcetera")
                    .version("0.1.1")
                    .classifier("features")
                    .type("xml");


    @Configuration
    public static Option[] withDefaultConfig(final Option ...options) {
        Option[] defaultOptions = {
                keepCaches(),
                karafDistributionConfiguration()
                        .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf-minimal").type("zip")
                                             .version("4.0.6"))
                        .karafVersion("4.0.6")
                        .unpackDirectory(new File("target/exam")),
                logLevel(WARN),
                features(karafStandardRepo),
                features(etceteraRepo, "etcetera"),
                junitBundles(),
                systemPackages("javax.mail","javax.mail.internet"),
                keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole().ignoreRemoteShell()
        };
        return Stream.concat(Arrays.stream(defaultOptions), Arrays.stream(options)).collect(Collectors.toList())
                     .toArray(defaultOptions);
    }



}