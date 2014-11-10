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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.builder.classloader;

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
    private Map<URI, AtomicInteger> counters = new ConcurrentHashMap<>();

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