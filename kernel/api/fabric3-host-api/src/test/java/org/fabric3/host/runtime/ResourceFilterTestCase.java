package org.fabric3.host.runtime;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
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
