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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
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

    public boolean canProcess(URL url) {
        return url.toString().endsWith(".contribution");
    }

    public List<URL> process(URL url, List<Library> libraries) throws IOException {
        List<URL> classpath = new ArrayList<>();
        File root = deReferenceFile(url);
        classpath.add(root.toURI().toURL());
        File metaInfLib = new File(root, "META-INF" + File.separator + "lib");
        if (metaInfLib.exists()) {
            classpath.add(metaInfLib.toURI().toURL());
        }
        File webInfLib = new File(root, "WEB-INF" + File.separator + "lib");
        if (webInfLib.exists()) {
            classpath.add(webInfLib.toURI().toURL());
        }
        File webInfClasses = new File(root, "WEB-INF" + File.separator + "classes");
        if (webInfClasses.exists()) {
            classpath.add(webInfClasses.toURI().toURL());
        }
        return classpath;
    }

    private File deReferenceFile(URL url) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(url.openStream());
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String line = bufferedReader.readLine().trim();
        return new File(line);
    }

}