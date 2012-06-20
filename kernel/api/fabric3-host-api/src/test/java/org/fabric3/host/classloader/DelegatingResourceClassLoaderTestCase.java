package org.fabric3.host.classloader;

import java.net.URL;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class DelegatingResourceClassLoaderTestCase extends TestCase {

    public void testGetResource() throws Exception {
        DelegatingResourceClassLoader classLoader = new DelegatingResourceClassLoader(new URL[0], getClass().getClassLoader());
        assertNotNull(classLoader.getResource(getClass().getName().replace(".", "/") + ".class"));
    }

    public void testGetResources() throws Exception {
        DelegatingResourceClassLoader classLoader = new DelegatingResourceClassLoader(new URL[0], getClass().getClassLoader());
        assertTrue(classLoader.getResources(getClass().getName().replace(".", "/") + ".class").hasMoreElements());
    }

}
