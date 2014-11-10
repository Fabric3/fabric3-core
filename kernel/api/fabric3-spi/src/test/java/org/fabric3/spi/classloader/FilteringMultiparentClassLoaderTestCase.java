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
package org.fabric3.spi.classloader;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 *
 */
public class FilteringMultiparentClassLoaderTestCase extends TestCase {
    private static final URI NAME = URI.create("test");

    public void testAllowPackage() throws Exception {
        Set<String> filters = new HashSet<>();
        filters.add(this.getClass().getPackage().getName() + ".*");
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(NAME, getClass().getClassLoader(), filters);
        assertNotNull(cl.loadClass(this.getClass().getName()));
    }

    public void testAllowWildcardPackage() throws Exception {
        Set<String> filters = new HashSet<>();
        filters.add("org.fabric3.*");
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(NAME, getClass().getClassLoader(), filters);
        assertNotNull(cl.loadClass(this.getClass().getName()));
    }

    public void testDisAllowParentPackage() throws Exception {
        Set<String> filters = new HashSet<>();
        filters.add("org.fabric3.jpa.someother.*");
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(NAME, getClass().getClassLoader(), filters);
        try {
            cl.loadClass(this.getClass().getName());
            fail();
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    public void testNoneAllowed() throws Exception {
        Set<String> filters = Collections.emptySet();
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(NAME, getClass().getClassLoader(), filters);
        try {
            cl.loadClass(this.getClass().getName());
            fail();
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    public void testFilterNoPackage() throws Exception {
        Set<String> set = Collections.emptySet();
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(NAME, getClass().getClassLoader(), set);
        try {
            cl.loadClass("Foo");
            fail();
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

}
