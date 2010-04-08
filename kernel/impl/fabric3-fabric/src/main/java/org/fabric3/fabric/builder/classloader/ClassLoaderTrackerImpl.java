/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.builder.classloader;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * Default implementation of ClassLoaderTracker.
 * <p/>
 * Note the MetaDataStore is used to determine if a contribution classloader must be tracked. If a contribution is registered in the store, it is
 * installed as an extension of the base runtime distribution and should only be uninstalled explicitly. Therefore, it does not need to be tracked.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderTrackerImpl implements ClassLoaderTracker {
    private MetaDataStore metaDataStore;

    private Map<URI, AtomicInteger> counter = new ConcurrentHashMap<URI, AtomicInteger>();

    public ClassLoaderTrackerImpl(@Reference MetaDataStore metaDataStore) {
        this.metaDataStore = metaDataStore;
    }

    public boolean isReferenced(URI uri) {
        return (metaDataStore.find(uri) != null) || (counter.get(uri) != null);
    }

    public void increment(URI uri) {
        if (metaDataStore.find(uri) == null) {
            AtomicInteger count = counter.get(uri);
            if (count == null) {
                count = new AtomicInteger(1);
                counter.put(uri, count);
            } else {
                count.incrementAndGet();
            }
        }
    }

    public void incrementImported(ClassLoader classLoader) {
        if (classLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            URI uri = cl.getName();
            if (metaDataStore.find(uri) == null) {
                AtomicInteger count = counter.get(uri);
                if (count == null) {
                    // this is a programming error as target classloaders should have been built before this and the counter incremented
                    throw new AssertionError("Target counter not found: " + uri);
                } else {
                    count.incrementAndGet();
                }
                for (ClassLoader parent : cl.getParents()) {
                    if (parent != null) {
                        incrementImported(parent);
                    }
                }
            }
        } else {
            ClassLoader parent = classLoader.getParent();
            if (parent != null) {
                incrementImported(parent);
            }
        }
    }

    public boolean decrement(ClassLoader classLoader) {
        if (classLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            boolean result = decrementCount(cl);
            for (ClassLoader parent : cl.getParents()) {
                decrement(parent);
            }
            return result;
        } else {
            ClassLoader parent = classLoader.getParent();
            if (parent != null) {
                decrement(parent);
            }
            return false;
        }
    }

    private boolean decrementCount(MultiParentClassLoader multiparent) {
        URI uri = multiparent.getName();
        AtomicInteger count = counter.get(uri);
        if (count != null) {
            if (count.decrementAndGet() == 0) {
                counter.remove(uri);
                return true;
            }
        }
        return false;
    }


}