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
package org.fabric3.contribution.wire;

import java.net.URL;

import org.fabric3.api.host.classloader.DelegatingResourceClassLoader;

/**
 * A bridging classloader that filters class and resource loading to a specified set of classes. This is used to enforce the semantics of a
 * JavaContributionWire.
 */
public class ClassLoaderWireFilter extends DelegatingResourceClassLoader {
    private static final URL[] NO_URLS = new URL[0];
    private String[] importedPackage;

    /**
     * Constructor.
     *
     * @param parent          the parent classloader.
     * @param importedPackage the package the wire imports
     */
    public ClassLoaderWireFilter(ClassLoader parent, String importedPackage) {
        super(NO_URLS, parent);
        this.importedPackage = importedPackage.split("\\.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        String[] clazz = name.split("\\.");
        for (int i = 0; i < importedPackage.length; i++) {
            String packageName = importedPackage[i];
            if ("*".equals(packageName)) {
                // wildcard reached, packages match
                break;
            } else if (clazz.length - 1 >= i && !packageName.equals(clazz[i])) {
                throw new ClassNotFoundException(name);
            }

        }
        return super.loadClass(name, resolve);
    }
}
