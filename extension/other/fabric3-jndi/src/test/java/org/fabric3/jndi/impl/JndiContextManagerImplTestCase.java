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
package org.fabric3.jndi.impl;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.Properties;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class JndiContextManagerImplTestCase extends TestCase {
    private JndiContextManagerImpl manager;
    private Properties properties;

    public void testRegisterUnregister() throws Exception {
        manager.register("context1", properties);
        assertNotNull(manager.get("context1"));
        manager.unregister("context1");
        assertNull(manager.get("context1"));
    }

    public void testDuplicateContext() throws Exception {
        manager.register("context1", properties);
        try {
            manager.register("context1", properties);
            fail();
        } catch (NamingException e) {
            // expected
        }
    }

    public void testDestroy() throws Exception {
        MockInitialContextFactory.CONTEXT.close();
        EasyMock.replay(MockInitialContextFactory.CONTEXT);

        manager.register("context1", properties);
        manager.destroy();

        EasyMock.verify(MockInitialContextFactory.CONTEXT);
    }

    public void testLookup() throws Exception {
        EasyMock.expect(MockInitialContextFactory.CONTEXT.lookup("test")).andReturn("test");
        EasyMock.replay(MockInitialContextFactory.CONTEXT);

        manager.register("context1", properties);
        assertNotNull(manager.lookup(String.class, "test"));

        EasyMock.verify(MockInitialContextFactory.CONTEXT);
    }

    public void testLookupByName() throws Exception {
        CompositeName name = new CompositeName("test");

        EasyMock.expect(MockInitialContextFactory.CONTEXT.lookup(name)).andReturn("test");
        EasyMock.replay(MockInitialContextFactory.CONTEXT);

        manager.register("context1", properties);
        assertNotNull(manager.lookup(String.class, name));

        EasyMock.verify(MockInitialContextFactory.CONTEXT);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContextManagerMonitor monitor = EasyMock.createNiceMock(ContextManagerMonitor.class);
        EasyMock.replay(monitor);
        manager = new JndiContextManagerImpl(monitor);

        properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());

        // setup initial context
        new InitialContext(properties);

        EasyMock.reset(MockInitialContextFactory.CONTEXT);
    }

    public static class MockInitialContextFactory implements InitialContextFactory {
        public static Context CONTEXT;

        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            if (CONTEXT == null) {
                CONTEXT = EasyMock.createMock(Context.class);
            }
            return CONTEXT;
        }

    }
}
