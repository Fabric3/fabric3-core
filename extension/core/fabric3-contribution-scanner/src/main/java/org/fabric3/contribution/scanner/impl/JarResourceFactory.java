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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.contribution.scanner.spi.FileResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactory;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for contribution jars
 */
@EagerInit
public class JarResourceFactory implements FileSystemResourceFactory {

    public JarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().endsWith(".jar") && !file.getName().endsWith(".zip")) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file.getCanonicalPath());
            JarEntry entry = jarFile.getJarEntry("META-INF/sca-contribution.xml");
            if (entry == null) {
                return null;
            }
        } catch (FileNotFoundException e) {
            // no sca-contribution, ignore
            return null;
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return new FileResource(file);
    }
}
