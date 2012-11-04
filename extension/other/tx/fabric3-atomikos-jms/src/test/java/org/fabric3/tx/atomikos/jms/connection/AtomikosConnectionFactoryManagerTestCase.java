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

package org.fabric3.tx.atomikos.jms.connection;

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.management.ManagementService;

/**
 *
 */
public class AtomikosConnectionFactoryManagerTestCase extends TestCase {

    public void testRegisterXA() throws Exception {
        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(Object.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));

        MockConnectionFactory factory = EasyMock.createMock(MockConnectionFactory.class);

        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);

        Map<String, String> properties = new HashMap<String, String>();
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
        managementService.export(EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(Object.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));

        MockConnectionFactory factory = EasyMock.createMock(MockConnectionFactory.class);

        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);
        manager.register("factory", factory);
        assertNotNull(manager.get("factory"));
        manager.destroy();
        EasyMock.verify(managementService, factory);
    }

    public void testRegisterNonXA() throws Exception {
        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        ConnectionFactory factory = EasyMock.createMock(ConnectionFactory.class);
        EasyMock.replay(managementService, factory);

        AtomikosConnectionFactoryManager manager = new AtomikosConnectionFactoryManager(managementService);
        manager.register("factory", factory);
        assertNotNull(manager.get("factory"));
        manager.unregister("factory");
        manager.destroy();

        EasyMock.verify(managementService, factory);

    }

    private interface MockConnectionFactory extends ConnectionFactory, XAConnectionFactory {
    }


}