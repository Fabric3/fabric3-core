/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.framework;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;

/**
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
public class AbstractResourceServiceTestCase extends TestCase {
    private AbstractResourceService service;
    private AbstractResourceService hierarchicalPathService;

    public void testRootMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("method");
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/foo", "/", Verb.GET, method, null, null, null);
        service.onRootResourceExport(mapping);
        assertTrue(service.getSubresources().contains(mapping));
    }

    public void testParameterizedMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("paramMethod", String.class);
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/foo/param", "/", Verb.GET, method, null, null, null);
        service.onRootResourceExport(mapping);
        assertTrue(service.getSubresources().contains(mapping));
    }

    public void testSubMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("method");
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/foo/bar", "/", Verb.GET, method, null, null, null);
        service.onRootResourceExport(mapping);
        assertFalse(service.getSubresources().contains(mapping));
    }

    public void testHierarchicalRootMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("method");
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/subpath/foo", "/", Verb.GET, method, null, null, null);
        hierarchicalPathService.onRootResourceExport(mapping);
        assertTrue(hierarchicalPathService.getSubresources().contains(mapping));
    }

    public void testHierarchicalParameterizedMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("paramMethod", String.class);
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/subpath/foo/param", "/", Verb.GET, method, null, null, null);
        hierarchicalPathService.onRootResourceExport(mapping);
        assertTrue(hierarchicalPathService.getSubresources().contains(mapping));
    }

    public void testHierarchicalSubMapping() throws Exception {
        Method method = getClass().getDeclaredMethod("method");
        ResourceMapping mapping = new ResourceMapping("foo", "/runtime/subpath/foo/bar", "/", Verb.GET, method, null, null, null);
        hierarchicalPathService.onRootResourceExport(mapping);
        assertFalse(hierarchicalPathService.getSubresources().contains(mapping));
    }

    public void setUp() throws Exception {
        super.setUp();

        service = new AbstractResourceService() {

            @Override
            protected String getResourcePath() {
                return "/runtime";
            }

        };

        hierarchicalPathService = new AbstractResourceService() {

            @Override
            protected String getResourcePath() {
                return "/runtime/subpath";
            }

        };

    }

    private void method() {

    }

    private void paramMethod(String param) {

    }
}
