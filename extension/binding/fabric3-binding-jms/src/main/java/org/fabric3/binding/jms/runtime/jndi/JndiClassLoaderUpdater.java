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
 */
package org.fabric3.binding.jms.runtime.jndi;

import java.net.URI;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Makes the <code>javax.jms</code> and <code>javax.transaction</code> packages imported by the JMS extension visible to the dynamic JNDI extension
 * classloader. This is required since the client libraries of some JMS providers (e.g. WebLogic) bundle <code>javax</code> classes, resulting in
 * class cast exceptions as those bundled classes will not be the same as the <code>javax</code> classes loaded by the JMS extension classloader.
 * <p/>
 * Note the "correct" way to do this would be for the JNDI extension to import the packages directly. However, this would introduce the complexity of
 * requiring users to add a manifest to the /jndi directory. Adding the extension classloader as a parent expediently avoids this.
 */
@EagerInit
public class JndiClassLoaderUpdater {
    private ClassLoaderRegistry registry;

    public JndiClassLoaderUpdater(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        MultiParentClassLoader jndiClassLoader = (MultiParentClassLoader) registry.getClassLoader(URI.create("f3-jndi"));
        if (jndiClassLoader == null) {
            return;
        }
        MultiParentClassLoader extensionClassLoader = (MultiParentClassLoader) getClass().getClassLoader();
        for (ClassLoader classLoader : extensionClassLoader.getParents()) {
            jndiClassLoader.addParent(classLoader);
        }
    }
}
