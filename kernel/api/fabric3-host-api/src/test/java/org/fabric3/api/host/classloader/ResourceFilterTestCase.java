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
package org.fabric3.api.host.classloader;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 *
 */
public class ResourceFilterTestCase extends TestCase {

    public void testFilterResource() throws Exception {
        ResourceFilter filter = new ResourceFilter(new String[]{"foo", "bar"});
        assertNull(filter.filterResource(new URL("file://foo")));
        assertNotNull(filter.filterResource(new URL("file://baz")));
    }

    public void testNoMaskResource() throws Exception {
        ResourceFilter filter = new ResourceFilter(new String[0]);
        assertNotNull(filter.filterResource(new URL("file://foo")));
    }

    public void testFilterResources() throws Exception {
        ResourceFilter filter = new ResourceFilter(new String[]{"foo", "bar"});

        Enumeration<URL> enumeration = Collections.enumeration(Collections.singleton(new URL("file://foo")));
        assertFalse(filter.filterResources(enumeration).hasMoreElements());

        Enumeration<URL> enumeration2 = Collections.enumeration(Collections.singleton(new URL("file://baz")));
        assertTrue(filter.filterResources(enumeration2).hasMoreElements());
    }

    public void testNoMaskResources() throws Exception {
        ResourceFilter filter = new ResourceFilter(new String[0]);

        Enumeration<URL> enumeration = Collections.enumeration(Collections.singleton(new URL("file://foo")));
        assertTrue(filter.filterResources(enumeration).hasMoreElements());
    }

}
