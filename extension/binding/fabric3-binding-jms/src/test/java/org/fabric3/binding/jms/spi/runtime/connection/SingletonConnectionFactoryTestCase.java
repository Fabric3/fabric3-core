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
package org.fabric3.binding.jms.spi.runtime.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class SingletonConnectionFactoryTestCase extends TestCase {
    private SingletonConnectionFactory singletonFactory;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    public void testConnection() throws Exception {
        EasyMock.expect(connectionFactory.createConnection()).andReturn(connection);
        connection.setExceptionListener(EasyMock.isA(ExceptionListener.class));
        connection.start();
        connection.stop();
        connection.close();

        EasyMock.replay(connectionFactory, connection);

        Connection returned = singletonFactory.createConnection();
        returned.start();
        returned.stop();
        returned.close();  // stop and close should not be called on underlying connection

        singletonFactory.destroy();

        EasyMock.verify(connectionFactory, connection);  // stop and close should now be called on underlying connection
    }

    public void testConnectionResetOnException() throws Exception {
        EasyMock.expect(connectionFactory.createConnection()).andReturn(connection).times(2);
        connection.setExceptionListener(EasyMock.isA(ExceptionListener.class));
        EasyMock.expectLastCall().times(2);
        connection.close();
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(connectionFactory, connection);

        singletonFactory.createConnection();
        singletonFactory.onException(new JMSException("test"));
        singletonFactory.createConnection();

        singletonFactory.destroy();

        EasyMock.verify(connectionFactory, connection);
    }

    public void setUp() throws Exception {
        super.setUp();
        connectionFactory = EasyMock.createMock(ConnectionFactory.class);
        connection = EasyMock.createMock(Connection.class);
        singletonFactory = new SingletonConnectionFactory(connectionFactory, null);
    }
}
