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

package org.fabric3.tx.atomikos.jms.connection;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.management.ManagementService;

/**
 *
 */
public class AtomikosConnectionFactoryManagerTestCase extends TestCase {

    public void testRegisterXA() throws Exception {
        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(Object.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));

        MockConnectionFactory factory = EasyMock.createMock(MockConnectionFactory.class);

        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);

        Map<String, String> properties = new HashMap<>();
        properties.put("borrow.timeout", "10000");
        properties.put("maintenance.interval", "10000");
        properties.put("max.idle", "10000");
        properties.put("pool.size", "10000");
        properties.put("pool.size.max", "10000");
        properties.put("pool.size.min", "10000");
        properties.put("reap.timeout", "10000");
        properties.put("local.transaction.mode", "false");

        manager.register("factory", factory, properties);
        assertNotNull(manager.get("factory"));
        manager.unregister("factory");
        manager.destroy();
        EasyMock.verify(managementService, factory);
    }

    public void testRegisterXANoUnregisterBeforeDestroy() throws Exception {
        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(Object.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));

        MockConnectionFactory factory = EasyMock.createMock(MockConnectionFactory.class);

        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);
        manager.register("factory", factory, Collections.<String, String>emptyMap());
        assertNotNull(manager.get("factory"));
        manager.destroy();
        EasyMock.verify(managementService, factory);
    }

    public void testRegisterNonXA() throws Exception {
        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        ConnectionFactory factory = EasyMock.createMock(ConnectionFactory.class);
        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);
        manager.register("factory", factory, Collections.<String, String>emptyMap());
        assertNotNull(manager.get("factory"));
        manager.unregister("factory");
        manager.destroy();

        EasyMock.verify(managementService, factory);

    }

    private interface MockConnectionFactory extends ConnectionFactory, XAConnectionFactory {
    }

}