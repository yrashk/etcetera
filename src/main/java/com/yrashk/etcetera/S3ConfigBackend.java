/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.yrashk.etcetera;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class S3ConfigBackend implements ConfigBackend {

    private final String bucket;
    private final String endpoint;
    private final String name;
    private final int order;
    private final AmazonS3Client client;

    public S3ConfigBackend(String bucket, String endpoint, String name, int order) {
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.name = name;
        this.order = order;
        client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        if (endpoint != null) {
            client.setEndpoint(endpoint);
        }
    }

    @Override public Collection<String> getFilenames() {
        Collection<String> names = new ArrayList<>();
        final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket);
        ListObjectsV2Result listing;
        do {
          listing = client.listObjectsV2(req);
          names.addAll(listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList()));
        } while (listing.isTruncated());

        return names;
    }

    @Override public InputStream getFileInputStream(String name) throws IOException {
        return client.getObject(bucket, name).getObjectContent();
    }

    @Override public void save(String name, String content) throws IOException {
        client.putObject(bucket, name, content);
    }

    @Override public int getOrder() {
        return order;
    }

    @Override public String getName() {
        return name;
    }
}
