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
package org.fabric3.contribution.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.ClasspathProcessor;
import org.fabric3.spi.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates the classpath for a a symbolic link contribution (*.contribution file). The contents of the file point to an exploded directory, which is placed on
 * the classpath.
 */
@EagerInit
public class SymLinkClasspathProcessor implements ClasspathProcessor {
    private static final String F3_SYMLINK = "f3.symlink";

    private final ClasspathProcessorRegistry registry;

    public SymLinkClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".contribution") || contribution.getMetaData(Boolean.class, F3_SYMLINK) != null;  // source url will change
    }

    public List<URL> process(Contribution contribution) throws Fabric3Exception {
        URL url = contribution.getLocation();
        List<URL> classpath = new ArrayList<>();

        try {
            addToClasspath(url, classpath);
            for (URL additional : contribution.getAdditionalLocations()) {
                addToClasspath(additional, classpath);
            }
            return classpath;
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    private void addToClasspath(URL url, List<URL> classpath) throws IOException {
        File root = new File(url.getFile());
        File metaInfLib = new File(root, "META-INF" + File.separator + "lib");
        boolean isResources = false;
        if (metaInfLib.exists()) {
            classpath.add(metaInfLib.toURI().toURL());
            isResources = true;
        }
        File webInfLib = new File(root, "WEB-INF" + File.separator + "lib");
        if (webInfLib.exists()) {
            classpath.add(webInfLib.toURI().toURL());
            isResources = true;
        }
        File webInfClasses = new File(root, "WEB-INF" + File.separator + "classes");
        if (webInfClasses.exists()) {
            classpath.add(webInfClasses.toURI().toURL());
            isResources = true;
        }
        if (!isResources) {
            classpath.add(url);
        }
    }

}