package org.fabric3.spi.util;

import junit.framework.TestCase;

public class ClassLoadingTest extends TestCase {

    public void testLoadPrimitive() throws Exception {
        assertEquals(Integer.TYPE, ClassLoading.loadClass(getClass().getClassLoader(), Integer.TYPE.getName()));
    }

    public void testLoadClassByClassLoader() throws Exception {
        assertEquals(Test.class, ClassLoading.loadClass(getClass().getClassLoader(), Test.class.getName()));
    }

    public static class Test {

    }

}