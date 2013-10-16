package org.fabric3.api.host.util;

import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 *
 */
public class CompositeEnumerationTestCase extends TestCase {

    public void testCompoundEnumeration() throws Exception {
        Enumeration<String> enumeration1 = Collections.enumeration(Collections.singleton("foo"));
        Enumeration<String> enumeration2 = Collections.enumeration(Collections.singleton("bar"));

        CompositeEnumeration<String> compositeEnumeration = new CompositeEnumeration<String>(new Enumeration[]{enumeration1, enumeration2});
        int times = 0;
        while (compositeEnumeration.hasMoreElements()) {
            times++;
            compositeEnumeration.nextElement();
        }
        assertEquals(2, times);
    }

    public void testSingleEnumeration() throws Exception {
        Enumeration<String> enumeration1 = Collections.enumeration(Collections.singleton("foo"));

        CompositeEnumeration<String> compositeEnumeration = new CompositeEnumeration<String>(new Enumeration[]{enumeration1});
        int times = 0;
        while (compositeEnumeration.hasMoreElements()) {
            times++;
            compositeEnumeration.nextElement();
        }
        assertEquals(1, times);
    }

    public void testEmptyEnumeration() throws Exception {
        CompositeEnumeration<String> compositeEnumeration = new CompositeEnumeration<String>(new Enumeration[0]);
        int times = 0;
        while (compositeEnumeration.hasMoreElements()) {
            times++;
            compositeEnumeration.nextElement();
        }
        assertEquals(0, times);
    }


}
