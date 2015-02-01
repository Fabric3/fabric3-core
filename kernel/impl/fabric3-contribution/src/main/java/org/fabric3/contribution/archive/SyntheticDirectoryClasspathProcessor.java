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
package org.fabric3.contribution.archive;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates the classpath for a contribution synthesized from a directory. All contained jars will be added to the classpath.
 */
@EagerInit
public class SyntheticDirectoryClasspathProcessor implements ClasspathProcessor {

    private final ClasspathProcessorRegistry registry;

    public SyntheticDirectoryClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
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
        if (!"file".equals(url.getProtocol())) {
            return false;
        }
        File root = FileHelper.toFile(url);
        return root.isDirectory();
    }

    public List<URL> process(URL url, List<Library> libraries) throws Fabric3Exception {
        try {
            List<URL> classpath = new ArrayList<>();
            File root = FileHelper.toFile(url);
            for (File file : root.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    classpath.add(file.toURI().toURL());
                }
            }
            return classpath;
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

}