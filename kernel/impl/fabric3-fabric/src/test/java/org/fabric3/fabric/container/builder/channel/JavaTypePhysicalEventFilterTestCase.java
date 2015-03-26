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
package org.fabric3.fabric.container.builder.channel;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 */
public class JavaTypePhysicalEventFilterTestCase extends TestCase {

    public void testFilter() throws Exception {
        JavaTypeEventFilter filter = new JavaTypeEventFilter(String.class);
        assertFalse(filter.filter(1));
    }

    public void testMultipleTypes() throws Exception {
        Class<?>[] types = new Class<?>[]{String.class, Integer.class};
        JavaTypeEventFilter filter = new JavaTypeEventFilter(types);
        assertTrue(filter.filter(1));
        assertTrue(filter.filter("test"));
        assertFalse(filter.filter(new ArrayList()));
    }

    public void testInheritence() throws Exception {
        JavaTypeEventFilter filter = new JavaTypeEventFilter(Foo.class);
        assertTrue(filter.filter(new Bar()));
    }

    private class Foo {

    }

    private class Bar extends Foo {

    }

}