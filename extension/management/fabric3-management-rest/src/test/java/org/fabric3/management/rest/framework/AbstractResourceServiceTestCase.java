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
package org.fabric3.management.rest.framework;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;

/**
 *
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
