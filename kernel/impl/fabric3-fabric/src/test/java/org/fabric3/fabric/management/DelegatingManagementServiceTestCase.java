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
package org.fabric3.fabric.management;

import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.model.type.java.ManagementInfo;
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
