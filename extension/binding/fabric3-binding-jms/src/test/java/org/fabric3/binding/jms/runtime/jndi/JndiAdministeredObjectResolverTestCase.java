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
package org.fabric3.binding.jms.runtime.jndi;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.jndi.spi.JndiContextManager;

/**
 *
 */
public class JndiAdministeredObjectResolverTestCase extends TestCase {
    private JndiAdministeredObjectResolver resolver;
    private JndiContextManager contextManager;
    private ConnectionFactoryManager factoryManager;

    public void testResolveConnectionFactory() throws Exception {
        ConnectionFactory factory = EasyMock.createMock(ConnectionFactory.class);
        EasyMock.expect(contextManager.lookup(ConnectionFactory.class, "test")).andReturn(factory);
        EasyMock.expect(factoryManager.register("test", factory)).andReturn(factory);
        EasyMock.replay(factory, contextManager, factoryManager);

        ConnectionFactoryDefinition definition = new ConnectionFactoryDefinition();
        definition.setName("test");
        assertNotNull(resolver.resolve(definition));

        EasyMock.verify(factory, contextManager, factoryManager);
    }

    public void testResolveDestination() throws Exception {
        Destination destination = EasyMock.createMock(Destination.class);
        EasyMock.expect(contextManager.lookup(Destination.class, "test")).andReturn(destination);
        EasyMock.replay(destination, contextManager, factoryManager);

        DestinationDefinition definition = new DestinationDefinition();
        definition.setName("test");
        assertNotNull(resolver.resolve(definition));

        EasyMock.verify(destination, factoryManager);
    }

    public void testResolveNotFound() throws Exception {
        EasyMock.expect(contextManager.lookup(ConnectionFactory.class, "test")).andReturn(null);
        EasyMock.replay(contextManager, factoryManager);

        ConnectionFactoryDefinition definition = new ConnectionFactoryDefinition();
        definition.setName("test");
        assertNull(resolver.resolve(definition));

        EasyMock.verify(contextManager, factoryManager);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        contextManager = EasyMock.createMock(JndiContextManager.class);
        factoryManager = EasyMock.createMock(ConnectionFactoryManager.class);

        resolver = new JndiAdministeredObjectResolver(contextManager, factoryManager);

    }


}
