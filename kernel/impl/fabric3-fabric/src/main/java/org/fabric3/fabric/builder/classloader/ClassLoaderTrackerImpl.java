/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Default implementation of ClassLoaderTracker.
 */
@EagerInit
public class ClassLoaderTrackerImpl implements ClassLoaderTracker {
    private Map<URI, AtomicInteger> counters = new ConcurrentHashMap<URI, AtomicInteger>();

    public boolean isReferenced(URI uri) {
        return counters.get(uri) != null;
    }

    public int increment(URI uri) {
        AtomicInteger count = counters.get(uri);
        if (count == null) {
            count = new AtomicInteger(1);
            counters.put(uri, count);
            return count.get();
        } else {
            return count.incrementAndGet();
        }
    }

    public void incrementImported(ClassLoader classLoader) {
        if (classLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            URI uri = cl.getName();
            AtomicInteger count = counters.get(uri);
            if (count == null) {
                count = new AtomicInteger(1);
                counters.put(uri, count);
            } else {
                count.incrementAndGet();
            }
            for (ClassLoader parent : cl.getParents()) {
                if (parent != null) {
                    incrementImported(parent);
                }
            }
        } else {
            ClassLoader parent = classLoader.getParent();
            if (parent != null) {
                incrementImported(parent);
            }
        }
    }

    public int decrement(ClassLoader classLoader) {
        if (classLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            int result = decrementCount(cl);
            for (ClassLoader parent : cl.getParents()) {
                decrement(parent);
            }
            return result;
        } else {
            ClassLoader parent = classLoader.getParent();
            if (parent != null) {
                decrement(parent);
            }
            return -1;
        }
    }

    private int decrementCount(MultiParentClassLoader classLoader) {
        URI uri = classLoader.getName();
        AtomicInteger count = counters.get(uri);
        if (count != null) {
            int val = count.decrementAndGet();
            if (val == 0) {
                counters.remove(uri);
            }
            return val;
        }
        return 0;
    }


}