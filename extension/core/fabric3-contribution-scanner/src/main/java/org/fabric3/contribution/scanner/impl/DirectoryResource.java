/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.scanner.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.contribution.scanner.spi.AbstractResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;

/**
 * A directory resource.
 */
public class DirectoryResource extends AbstractResource {
    private final File root;
    // the list of resources to track changes against
    private List<FileSystemResource> resources;

    public DirectoryResource(File root) {
        this.root = root;
        resources = new ArrayList<>();
    }

    public String getName() {
        return root.getName();
    }

    public long getTimestamp() {
        long latest = root.lastModified();
        for (FileSystemResource resource : resources) {
            long timestamp = resource.getTimestamp();
            if (timestamp > latest) {
                latest = timestamp;
            }
        }
        return latest;
    }

    public URL getLocation() {
        try {
            return root.toURI().normalize().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public void addResource(FileSystemResource resource) {
        resources.add(resource);
    }


}
