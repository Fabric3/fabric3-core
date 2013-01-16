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
*/
package org.fabric3.binding.activemq.provider;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryConfigurationParser;
import org.fabric3.binding.jms.spi.runtime.provider.InvalidConfigurationException;

/**
 * Parses {@link ActiveMQConnectionFactoryConfiguration} entries from a StAX source; entries may be connection factories or connection factory
 * templates.
 */
@EagerInit
public class ActiveMQConnectionFactoryConfigurationParser implements ConnectionFactoryConfigurationParser {
    private String defaultBrokerName;

    public ActiveMQConnectionFactoryConfigurationParser(@Reference BrokerHelper helper) {
        this.defaultBrokerName = helper.getDefaultBrokerName();
    }

    public ActiveMQConnectionFactoryConfiguration parse(XMLStreamReader reader) throws InvalidConfigurationException, XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            invalidConfiguration("Connection factory name not configured", reader, null);
        }
        ActiveMQConnectionFactoryConfiguration configuration = new ActiveMQConnectionFactoryConfiguration(name);

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
            configuration.setBrokerUri(uri);
        } catch (URISyntaxException e) {
            invalidConfiguration("Invalid broker URL", reader, e);
        }
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                String localPart = reader.getName().getLocalPart();
                if ("factory.properties".equals(localPart)) {
                    parseFactoryProperties(configuration, reader);
                } else if ("pool.properties".equals(localPart)) {
                    parsePoolProperties(configuration, reader);
                } else {
                    invalidConfiguration("Unrecognized element " + localPart + " in system configuration", reader, null);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (reader.getName().getLocalPart().startsWith("connection.factory")) {
                    return configuration;
                }
            }
        }
    }

    private void parseFactoryProperties(ActiveMQConnectionFactoryConfiguration configuration, XMLStreamReader reader) throws XMLStreamException {
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

    private void parsePoolProperties(ActiveMQConnectionFactoryConfiguration configuration, XMLStreamReader reader) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                configuration.setPoolProperty(reader.getName().getLocalPart(), reader.getElementText());
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("pool.properties".equals(reader.getName().getLocalPart())) {
                    return;
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                return;
            }
        }
    }

    private void invalidConfiguration(String message, XMLStreamReader reader, Exception e) throws InvalidConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            // runtime has no external config file
            if (e != null) {
                throw new InvalidConfigurationException(message, e);
            }
            throw new InvalidConfigurationException(message);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        if (e != null) {
            throw new InvalidConfigurationException(message + " [" + line + "," + col + "]", e);
        }
        throw new InvalidConfigurationException(message + " [" + line + "," + col + "]");
    }


}
