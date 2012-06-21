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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.loader;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.CreateOption;
import org.fabric3.binding.jms.spi.common.DeliveryMode;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.DestinationType;
import org.fabric3.binding.jms.spi.common.HeadersDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.JmsUri;
import org.fabric3.binding.jms.spi.common.MessageSelection;
import org.fabric3.binding.jms.spi.common.ResponseDefinition;

/**
 * Helper class for loading JMS binding configuration from a composite.
 */
public class JmsLoaderHelper {
    public final static String CONNECTION_FACORY_NAME = "jndiConnectionFactoryName";
    public final static String DESTINATION_TYPE = "destinationType";
    public final static String DELIVERY_MODE = "deliveryMode";
    public final static String TIME_TO_LIVE = "timeToLive";
    public final static String PRIORITY = "priority";
    public final static String RESPONSE_DESTINATION = "responseDestination";
    public final static String MESSAGE_SELECTOR = "selector";
    public static final String REPLY_TO_NAME = "replyToName";


    private JmsLoaderHelper() {
    }

    /**
     * Parses metadata from an input string.
     *
     * @param uri string for /binding.jms/@uri
     * @return a JmsURIMetadata
     * @throws JmsUriException when the URI is not a valid format required by /binding.jms/@uri.
     */
    public static JmsBindingMetadata parseUri(String uri) throws JmsUriException {
        boolean matches = Pattern.matches("jms:(.*?)[\\?(.*?)=(.*?)((&(.*?)=(.*?))*)]?", uri);
        if (!matches) {
            throw new JmsUriException("Invalid JMS URI: " + uri);
        }
        JmsUri jmsUri = doParse(uri);
        return getJmsMetadataFromUri(jmsUri);
    }

    /**
     * Parses the URI string
     *
     * @param uri the string to parse
     * @return the parsed URI
     */
    private static JmsUri doParse(String uri) {
        StringTokenizer token = new StringTokenizer(uri, ":?=&");
        String current;
        String propertyName = null;
        int pos = 0;
        JmsUri result = null;
        while (token.hasMoreTokens()) {
            current = token.nextToken();
            if (1 == pos) {
                result = new JmsUri(current);
            } else if (pos % 2 == 0) {
                propertyName = current;
            } else if (0 != pos) {// ignore beginning 'jms'
                assert propertyName != null;
                assert result != null;
                result.getProperties().put(propertyName.trim(), current.trim());
            }
            pos++;
        }
        return result;
    }

    /**
     * Transform a JmsURIMetadata object to a JmsBindingMetadata.
     *
     * @param jmsUri JmsURIMetadata
     * @return a equivalent JmsURIMetadata object
     * @throws JmsUriException if there is a syntax error in the URI
     */
    private static JmsBindingMetadata getJmsMetadataFromUri(JmsUri jmsUri) throws JmsUriException {
        JmsBindingMetadata metadata = new JmsBindingMetadata();
        Map<String, String> uriProperties = jmsUri.getProperties();

        // Destination
        DestinationDefinition destination = new DestinationDefinition();
        String destinationType = uriProperties.get(DESTINATION_TYPE);
        if ("topic".equalsIgnoreCase(destinationType)) {
            destination.setType(DestinationType.TOPIC);
        }
        destination.setName(jmsUri.getDestination());
        destination.setCreate(CreateOption.NEVER); // always assume the destination already exists
        metadata.setDestination(destination);

        // ConnectionFactory
        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
        String connectionFactoryName = uriProperties.get(CONNECTION_FACORY_NAME);
        connectionFactory.setName(connectionFactoryName);
        connectionFactory.setCreate(CreateOption.NEVER);
        metadata.setConnectionFactory(connectionFactory);

        // Response copy configuration of request
        ResponseDefinition response = new ResponseDefinition();
        response.setConnectionFactory(connectionFactory);
        DestinationDefinition responseDestinationDef = new DestinationDefinition();
        String responseDestination = uriProperties.get(RESPONSE_DESTINATION);
        responseDestinationDef.setName(responseDestination);
        responseDestinationDef.setCreate(CreateOption.NEVER);
        response.setDestination(responseDestinationDef);
        metadata.setResponse(response);

        HeadersDefinition headers = metadata.getUriHeaders();
        Map<String, String> properties = jmsUri.getProperties();
        String mode = properties.get(DELIVERY_MODE);
        if (mode != null) {
            if ("nonpersistent".equals(mode)) {
                headers.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            } else if ("persistent".equals(mode)) {
                headers.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                throw new JmsUriException("Invalid delivery mode specified: " + mode);
            }
        }
        String timeToLive = properties.get(TIME_TO_LIVE);
        if (timeToLive != null) {
            try {
                long val = Long.parseLong(timeToLive);
                headers.setTimeToLive(val);
            } catch (NumberFormatException e) {
                throw new JmsUriException("Invalid time to live: " + timeToLive, e);
            }
        }
        String priority = properties.get(PRIORITY);
        if (priority != null) {
            try {
                int val = Integer.parseInt(priority);
                if (val < 0 || val > 9) {
                    throw new JmsUriException("Priority must be between 0 and 9: " + timeToLive);
                }
                headers.setPriority(val);
            } catch (NumberFormatException e) {
                throw new JmsUriException("Invalid priority: " + priority, e);
            }
        }

        String selector = properties.get(MESSAGE_SELECTOR);
        if (selector != null) {
            MessageSelection selection = new MessageSelection(selector);
            metadata.setUriMessageSelection(selection);
        }

        String replyTo = properties.get(REPLY_TO_NAME);
        if (replyTo != null) {
            // TODO
        }

        return metadata;

    }

}
