/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.host.classloader;

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
