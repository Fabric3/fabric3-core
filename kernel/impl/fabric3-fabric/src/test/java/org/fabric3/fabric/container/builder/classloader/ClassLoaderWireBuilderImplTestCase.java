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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 *
 */
public class ClassLoaderWireBuilderImplTestCase extends TestCase {
    private static final String PACKAGE = "org.fabric3.test";

    public void testBuildFilter() throws Exception {
        URI sourceUri = URI.create("source");
        MultiParentClassLoader source = new MultiParentClassLoader(sourceUri, getClass().getClassLoader());
        URI parentUri = URI.create("parent");
        MultiParentClassLoader parent = new MultiParentClassLoader(parentUri, getClass().getClassLoader());

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(parentUri)).andReturn(parent);
        EasyMock.replay(registry);
        ClassLoaderWireBuilderImpl builder = new ClassLoaderWireBuilderImpl(registry);

        PhysicalClassLoaderWireDefinition definition = new PhysicalClassLoaderWireDefinition(parentUri, PACKAGE);

        builder.build(source, definition);

        ClassLoader filter = source.getParents().get(1);
        assertTrue(filter instanceof ClassLoaderWireFilter);

        Field field = ClassLoaderWireFilter.class.getDeclaredField("importedPackage");
        field.setAccessible(true);
        assertEquals(3, Array.getLength(field.get(filter)));   // 3 is the lengnth of org.fabric3.test tokenized
        assertEquals(parent, filter.getParent());
        EasyMock.verify(registry);
    }

    public void testBuildNoFilter() throws Exception {
        URI sourceUri = URI.create("source");
        MultiParentClassLoader source = new MultiParentClassLoader(sourceUri, getClass().getClassLoader());
        URI parentUri = URI.create("parent");
        MultiParentClassLoader parent = new MultiParentClassLoader(parentUri, getClass().getClassLoader());

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(parentUri)).andReturn(parent);
        EasyMock.replay(registry);
        ClassLoaderWireBuilderImpl builder = new ClassLoaderWireBuilderImpl(registry);

        PhysicalClassLoaderWireDefinition definition = new PhysicalClassLoaderWireDefinition(parentUri, null); // no filter

        builder.build(source, definition);

        assertEquals(parent, source.getParents().get(1));
        EasyMock.verify(registry);
    }
}
