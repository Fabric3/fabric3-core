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

import org.fabric3.contribution.scanner.spi.FileResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactory;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates a FileResource for exploded SCA contribution jars
 */
@EagerInit
public class ExplodedJarResourceFactory implements FileSystemResourceFactory {

    public ExplodedJarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.isDirectory()) {
            return null;
        }
        File manifest = new File(file, "/META-INF/sca-contribution.xml");
        if (!manifest.exists()) {
            // not a contribution archive, ignore
            return null;
        }
        DirectoryResource directoryResource = new DirectoryResource(file);
        // monitor everything in META-INF
        File metaInf = new File(file, "/META-INF");
        monitorResource(directoryResource, metaInf);
        return directoryResource;
    }

    private void monitorResource(DirectoryResource directoryResource, File file) {
        if (file.isDirectory()) {
            for (File entry : file.listFiles()) {
                if (entry.isFile()) {
                    directoryResource.addResource(new FileResource(entry));
                } else {
                    monitorResource(directoryResource, entry);
                }
            }
        } else {
            directoryResource.addResource(new FileResource(file));
        }

    }
}
