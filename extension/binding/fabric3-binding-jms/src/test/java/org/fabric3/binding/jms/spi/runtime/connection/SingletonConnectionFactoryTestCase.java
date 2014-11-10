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
