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
package org.fabric3.fabric.classloader;

import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 *
 */
public class ClassLoaderRegistryImplTestCase extends TestCase {
    private static final URI CLassLOADER_URI = URI.create("classloader");

    private ClassLoaderRegistry registry;

    public void testLoadPrimitive() throws Exception {
        assertEquals(Integer.TYPE, registry.loadClass(getClass().getClassLoader(), Integer.TYPE.getName()));
    }

    public void testLoadClassByClassLoader() throws Exception {
        assertEquals(Test.class, registry.loadClass(getClass().getClassLoader(), Test.class.getName()));
    }

    public void testLoadClassByUri() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        registry.register(CLassLOADER_URI, loader);
        assertEquals(Test.class, registry.loadClass(CLassLOADER_URI, Test.class.getName()));
    }

    public void testRegisterUnregister() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        registry.register(CLassLOADER_URI, loader);
        assertEquals(loader, registry.getClassLoader(CLassLOADER_URI));
        assertEquals(loader, registry.unregister(CLassLOADER_URI));
        assertNull(registry.getClassLoader(CLassLOADER_URI));
        registry.register(CLassLOADER_URI, loader);
    }

    public void testDuplicateRegistration() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        registry.register(CLassLOADER_URI, loader);
        try {
            registry.register(CLassLOADER_URI, loader);
            fail();
        } catch (AssertionError e) {
            // expected
        }
    }

    public void testGetClassLoaders() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        registry.register(CLassLOADER_URI, loader);
        assertEquals(1, registry.getClassLoaders().size());
        assertTrue(registry.getClassLoaders().containsValue(loader));
    }

    public void testGetClassLoader() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        registry.register(CLassLOADER_URI, loader);
        assertEquals(loader, registry.getClassLoader(CLassLOADER_URI));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new ClassLoaderRegistryImpl();
    }

    private static class Test {

    }
}
