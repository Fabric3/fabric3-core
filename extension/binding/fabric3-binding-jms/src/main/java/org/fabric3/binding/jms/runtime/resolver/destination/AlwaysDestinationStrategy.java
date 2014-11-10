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
package org.fabric3.binding.jms.runtime.resolver.destination;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TopicConnection;

import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.binding.jms.runtime.resolver.DestinationStrategy;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.spi.runtime.provider.JmsResolutionException;

/**
 * Implementation that attempts to always create the destination.
 */
public class AlwaysDestinationStrategy implements DestinationStrategy {

    public Destination getDestination(DestinationDefinition definition, ConnectionFactory factory) throws JmsResolutionException {
        Connection connection = null;
        String name = definition.getName();
        try {
            connection = factory.createConnection();
            connection.start();
            switch (definition.geType()) {
            case QUEUE:
                QueueConnection qc = (QueueConnection) connection;
                return qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE).createQueue(name);
            case TOPIC:
                TopicConnection tc = (TopicConnection) connection;
                return tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE).createTopic(name);
            default:
                throw new IllegalArgumentException("Unknown destination type for:" + name);
            }
        } catch (JMSException ex) {
            throw new JmsResolutionException("Unable to create destination:" + name, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.stop();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            JmsHelper.closeQuietly(connection);
        }
    }
}
