/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.jms.runtime.jndi;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;

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
