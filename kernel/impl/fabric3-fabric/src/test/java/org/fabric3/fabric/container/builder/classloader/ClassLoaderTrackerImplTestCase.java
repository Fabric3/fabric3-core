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
package org.fabric3.fabric.container.builder.classloader;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 *
 */
public class ClassLoaderTrackerImplTestCase extends TestCase {
    private static final URL[] EMPTY_URLS = new URL[0];

    private ClassLoaderTrackerImpl tracker;

    public void testTransitiveTracking() throws Exception {
        URI grandParent1Uri = URI.create("grandparent1");
        URI grandParent2Uri = URI.create("grandparent2");
        URI classLoader1Uri = URI.create("cl1");
        URI classLoader2Uri = URI.create("cl2");

        ClassLoader topLevel = new URLClassLoader(EMPTY_URLS);
        MultiParentClassLoader grandParent1 = new MultiParentClassLoader(grandParent1Uri, topLevel);
        MultiParentClassLoader grandParent2 = new MultiParentClassLoader(grandParent2Uri, topLevel);
        ClassLoader parent1 = new URLClassLoader(EMPTY_URLS, grandParent1);
        ClassLoader parent2 = new URLClassLoader(EMPTY_URLS, grandParent2);

        MultiParentClassLoader classLoader1 = new MultiParentClassLoader(classLoader1Uri, parent1);
        classLoader1.addParent(parent2);

        MultiParentClassLoader classLoader2 = new MultiParentClassLoader(classLoader2Uri, parent1);
        classLoader2.addParent(parent2);

        tracker.increment(grandParent1Uri);
        tracker.increment(grandParent2Uri);

        tracker.increment(classLoader1Uri);
        tracker.incrementImported(parent1);
        tracker.incrementImported(parent2);

        tracker.increment(classLoader2Uri);
        tracker.incrementImported(parent1);
        tracker.incrementImported(parent2);

        assertTrue(tracker.isReferenced(classLoader1Uri));
        assertTrue(tracker.isReferenced(classLoader2Uri));

        assertTrue(tracker.decrement(classLoader1) == 0);
        assertTrue(tracker.isReferenced(grandParent1Uri));
        assertTrue(tracker.isReferenced(grandParent2Uri));

        assertTrue(tracker.decrement(classLoader2) == 0);
        assertTrue(tracker.isReferenced(grandParent1Uri));
        assertTrue(tracker.isReferenced(grandParent2Uri));

        assertTrue(tracker.decrement(grandParent1) == 0);
        assertTrue(tracker.decrement(grandParent2) == 0);
    }

    public void testTransitiveTrackingUnloadGrandParents() throws Exception {
        URI grandParent1Uri = URI.create("grandparent1");
        URI grandParent2Uri = URI.create("grandparent2");
        URI classLoader1Uri = URI.create("cl1");
        URI classLoader2Uri = URI.create("cl2");

        ClassLoader topLevel = new URLClassLoader(EMPTY_URLS);
        MultiParentClassLoader grandParent1 = new MultiParentClassLoader(grandParent1Uri, topLevel);
        MultiParentClassLoader grandParent2 = new MultiParentClassLoader(grandParent2Uri, topLevel);
        ClassLoader parent1 = new URLClassLoader(EMPTY_URLS, grandParent1);
        ClassLoader parent2 = new URLClassLoader(EMPTY_URLS, grandParent2);

        MultiParentClassLoader classLoader1 = new MultiParentClassLoader(classLoader1Uri, parent1);
        classLoader1.addParent(parent2);

        MultiParentClassLoader classLoader2 = new MultiParentClassLoader(classLoader2Uri, parent1);
        classLoader2.addParent(parent2);

        tracker.increment(grandParent1Uri);
        tracker.increment(grandParent2Uri);

        tracker.increment(classLoader1Uri);
        tracker.incrementImported(parent1);
        tracker.incrementImported(parent2);

        tracker.increment(classLoader2Uri);
        tracker.incrementImported(parent1);
        tracker.incrementImported(parent2);

        // decrement imported contributions before the importing contributions
        tracker.decrement(grandParent1);
        tracker.decrement(grandParent2);

        assertTrue(tracker.isReferenced(grandParent1Uri));
        assertTrue(tracker.isReferenced(grandParent2Uri));
        assertTrue(tracker.isReferenced(classLoader1Uri));
        assertTrue(tracker.isReferenced(classLoader2Uri));

        assertTrue(tracker.decrement(classLoader1) == 0);

        assertTrue(tracker.isReferenced(grandParent1Uri));
        assertTrue(tracker.isReferenced(grandParent2Uri));

        assertTrue(tracker.decrement(classLoader2) == 0);

        assertFalse(tracker.isReferenced(grandParent1Uri));
        assertFalse(tracker.isReferenced(grandParent2Uri));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tracker = new ClassLoaderTrackerImpl();
    }
}
