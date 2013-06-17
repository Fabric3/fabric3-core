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
package org.fabric3.jndi.impl;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

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
