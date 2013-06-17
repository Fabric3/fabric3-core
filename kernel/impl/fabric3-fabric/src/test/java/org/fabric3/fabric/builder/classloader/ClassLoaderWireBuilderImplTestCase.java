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
*/
package org.fabric3.fabric.builder.classloader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;

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
