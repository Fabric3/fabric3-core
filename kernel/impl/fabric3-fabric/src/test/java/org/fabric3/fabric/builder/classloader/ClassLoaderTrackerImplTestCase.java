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
*/
package org.fabric3.fabric.builder.classloader;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * @version $Rev$ $Date$
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
