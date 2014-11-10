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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.activemq.provider;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.introspection.ConnectionFactoryConfigurationParser;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Parses {@link ConnectionFactoryConfiguration} entries from a StAX source; entries may be connection factories or connection factory templates.
 */
@EagerInit
public class ActiveMQConnectionFactoryConfigurationParser implements ConnectionFactoryConfigurationParser {
    private String defaultBrokerName;

    public ActiveMQConnectionFactoryConfigurationParser(@Reference BrokerHelper helper) {
        this.defaultBrokerName = helper.getDefaultBrokerName();
    }

    public ConnectionFactoryConfiguration parse(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        Location location = reader.getLocation();
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Connection factory name not configured", location);
            context.addError(error);
        }
        ConnectionFactoryConfiguration configuration = new ConnectionFactoryConfiguration(name, "activemq");

        String clientId = reader.getAttributeValue(null, "client.id");
        configuration.setClientId(clientId);

        String username = reader.getAttributeValue(null, "username");
        configuration.setUsername(username);
        String password = reader.getAttributeValue(null, "password");
        configuration.setPassword(password);

        String typeString = reader.getAttributeValue(null, "type");
        if (typeString != null) {
            ConnectionFactoryType type = ConnectionFactoryType.valueOf(typeString.trim().toUpperCase());
            configuration.setType(type);
        }
        String urlString = reader.getAttributeValue(null, "broker.url");
        if (urlString == null) {
            urlString = defaultBrokerName;
        }
        try {
            URI uri = new URI(urlString);
            configuration.addAttribute("broker.uri", uri);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid broker URL", location, e);
            context.addError(error);
        }
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    String localPart = reader.getName().getLocalPart();
                    if ("factory.properties".equals(localPart)) {
                        parseFactoryProperties(configuration, reader);
                    } else {
                        UnrecognizedAttribute error = new UnrecognizedAttribute("Unrecognized element " + localPart + " in system configuration", location);
                        context.addError(error);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (reader.getName().getLocalPart().startsWith("connection.factory")) {
                        return configuration;
                    }
            }
        }
    }

    private void parseFactoryProperties(ConnectionFactoryConfiguration configuration, XMLStreamReader reader) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    configuration.setFactoryProperty(reader.getName().getLocalPart(), reader.getElementText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("factory.properties".equals(reader.getName().getLocalPart())) {
                        return;
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
            }
        }
    }

}
