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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.management;

import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;

/**
 *
 */
public class DelegatingManagementServiceTestCase extends TestCase {
    private DelegatingManagementService managementService = new DelegatingManagementService();

    public void testComponentRegistration() throws Exception {
        URI uri = URI.create("test");
        SingletonObjectFactory<Object> factory = new SingletonObjectFactory<Object>(this);
        ClassLoader loader = getClass().getClassLoader();

        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export(uri, null, factory, loader);
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.setExtensions(Collections.singletonList(extension));

        managementService.export(uri, null, factory, loader);

        EasyMock.verify(extension);
    }

    public void testComponentRemove() throws Exception {
        URI uri = URI.create("test");
        SingletonObjectFactory<Object> factory = new SingletonObjectFactory<Object>(this);
        ClassLoader loader = getClass().getClassLoader();
        ManagementInfo info = new ManagementInfo(null, null, null, null, null, null, null);

        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export(uri, info, factory, loader);
        EasyMock.expectLastCall();
        extension.remove(uri, info);
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.setExtensions(Collections.singletonList(extension));

        managementService.export(uri, info, factory, loader);
        managementService.remove(uri, info);

        EasyMock.verify(extension);
    }

    public void testDelayedComponentRegistration() throws Exception {
        URI uri = URI.create("test");
        SingletonObjectFactory<Object> factory = new SingletonObjectFactory<Object>(this);
        ClassLoader loader = getClass().getClassLoader();

        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export(uri, null, factory, loader);
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.export(uri, null, factory, loader);

        managementService.setExtensions(Collections.singletonList(extension));
        EasyMock.verify(extension);
    }


    public void testInstanceRegistration() throws Exception {
        Object instance = new Object();
        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export("name", "group", "desc", instance);
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.setExtensions(Collections.singletonList(extension));

        managementService.export("name", "group", "desc", instance);

        EasyMock.verify(extension);
    }

    public void testInstanceRemove() throws Exception {
        Object instance = new Object();
        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export("name", "group", "desc", instance);
        EasyMock.expectLastCall();
        extension.remove("name", "group");
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.setExtensions(Collections.singletonList(extension));

        managementService.export("name", "group", "desc", instance);
        managementService.remove("name", "group");

        EasyMock.verify(extension);
    }

    public void testDelayedInstanceRegistration() throws Exception {
        Object instance = new Object();
        ManagementExtension extension = EasyMock.createMock(ManagementExtension.class);
        EasyMock.expect(extension.getType()).andReturn("test.extension");
        extension.export("name", "group", "desc", instance);
        EasyMock.expectLastCall();
        EasyMock.replay(extension);

        managementService.export("name", "group", "desc", instance);

        managementService.setExtensions(Collections.singletonList(extension));

        EasyMock.verify(extension);

    }

}
