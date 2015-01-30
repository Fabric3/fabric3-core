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
package org.fabric3.binding.jms.runtime.jndi;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import java.util.Map;

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

    @SuppressWarnings("unchecked")
    public void testResolveConnectionFactory() throws Exception {
        ConnectionFactory factory = EasyMock.createMock(ConnectionFactory.class);
        EasyMock.expect(contextManager.lookup(ConnectionFactory.class, "test")).andReturn(factory);
        EasyMock.expect(factoryManager.register( EasyMock.eq("test"), EasyMock.eq(factory), EasyMock.isA(Map.class))).andReturn(factory);
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
