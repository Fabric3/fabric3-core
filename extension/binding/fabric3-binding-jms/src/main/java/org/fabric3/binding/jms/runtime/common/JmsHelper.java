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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime.common;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * Utility class for managing JMS objects.
 */
public class JmsHelper {

    private JmsHelper() {
    }

    /**
     * Closes the connection quietly, ignoring exceptions.
     *
     * @param connection the connection to be closed.
     */
    public static void closeQuietly(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes the session quietly, ignoring exceptions.
     *
     * @param session the session to be closed.
     */
    public static void closeQuietly(Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes the message consumer quietly, ignoring exceptions
     *
     * @param consumer the message consumer to be closed.
     */
    public static void closeQuietly(MessageConsumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException ignore) {
        }
    }

}
