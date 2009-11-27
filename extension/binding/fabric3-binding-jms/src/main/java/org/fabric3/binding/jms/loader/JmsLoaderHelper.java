/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.loader;

import java.util.Map;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.JmsURIMetadata;
import org.fabric3.binding.jms.common.ResponseDefinition;

/**
 * Helper class for loading JMS binding configuration from a comppsite.
 */
public class JmsLoaderHelper {
    private static final String DEFAULT_CLIENT_QUEUE = "clientQueue";
    private static final String DEFAULT_JMS_CONNECTION_FACTORY = "connectionFactory";

    private JmsLoaderHelper() {
    }

    /**
     * Transform a JmsURIMetadata object to a JmsBindingMetadata.
     *
     * @param uriMeta JmsURIMetadata
     * @return a equivalent JmsURIMetadata object
     */
    static JmsBindingMetadata getJmsMetadataFromURI(JmsURIMetadata uriMeta) {
        JmsBindingMetadata result = new JmsBindingMetadata();
        Map<String, String> uriProperties = uriMeta.getProperties();

        // Destination
        DestinationDefinition destination = new DestinationDefinition();
        String destinationType = uriProperties.get(JmsURIMetadata.DESTINATIONTYPE);
        if ("topic".equalsIgnoreCase(destinationType)) {
            destination.setType(DestinationType.TOPIC);
        }
        destination.setName(uriMeta.getDestination());
        destination.setCreate(CreateOption.NEVER); // always assume the destination already exists
        result.setDestination(destination);

        // ConnectionFactory
        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
        String connectionFactoryName = uriProperties
                .get(JmsURIMetadata.CONNECTIONFACORYNAME);
        if (connectionFactoryName == null) {
            connectionFactory.setName(DEFAULT_JMS_CONNECTION_FACTORY);
        } else {
            connectionFactory.setName(connectionFactoryName);
        }
        connectionFactory.setCreate(CreateOption.NEVER);
        result.setConnectionFactory(connectionFactory);

        // Response copy configuration of request
        ResponseDefinition response = new ResponseDefinition();
        response.setConnectionFactory(connectionFactory);
        DestinationDefinition responseDestinationDef = new DestinationDefinition();
        String responseDestination = uriProperties.get(JmsURIMetadata.RESPONSEDESTINAT);
        if (responseDestination != null) {
            responseDestinationDef.setName(responseDestination);
        } else {
            responseDestinationDef.setName(DEFAULT_CLIENT_QUEUE);

        }
        responseDestinationDef.setCreate(CreateOption.NEVER);
        response.setDestination(responseDestinationDef);
        result.setResponse(response);
        return result;
    }

}
